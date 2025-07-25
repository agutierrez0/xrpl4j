package org.xrpl.xrpl4j.tests;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: integration-tests
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.awaitility.Durations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.crypto.JavaKeystoreLoader;
import org.xrpl.xrpl4j.crypto.ServerSecret;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PrivateKeyReference;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.bc.BcDerivedKeySignatureService;
import org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService;
import org.xrpl.xrpl4j.model.client.Finality;
import org.xrpl.xrpl4j.model.client.FinalityStatus;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountChannelsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountChannelsResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams.AccountObjectType;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsResult;
import org.xrpl.xrpl4j.model.client.accounts.TrustLine;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.CredentialLedgerEntryParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerResult;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindRequestParams;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.flags.TrustSetFlags;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.ledger.PermissionedDomainObject;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Credential;
import org.xrpl.xrpl4j.model.transactions.CredentialAccept;
import org.xrpl.xrpl4j.model.transactions.CredentialCreate;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.CredentialWrapper;
import org.xrpl.xrpl4j.model.transactions.DepositPreAuth;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.PermissionedDomainSet;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.TransactionType;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.tests.environment.XrplEnvironment;

import java.security.Key;
import java.security.KeyStore;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * An abstract class that contains helper functionality to support all ITs.
 */
public abstract class AbstractIT {

  public static final Duration POLL_INTERVAL = Durations.ONE_HUNDRED_MILLISECONDS;
  public static final Duration AT_MOST_INTERVAL = Duration.of(30, ChronoUnit.SECONDS);
  public static final String SUCCESS_STATUS = TransactionResultCodes.TES_SUCCESS;

  protected static XrplEnvironment xrplEnvironment = XrplEnvironment.getNewConfiguredEnvironment();

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  protected final XrplClient xrplClient;

  protected final SignatureService<PrivateKey> signatureService;
  protected final SignatureService<PrivateKeyReference> derivedKeySignatureService;

  /**
   * No-args Constructor.
   */
  protected AbstractIT() {
    this.xrplClient = xrplEnvironment.getXrplClient();
    this.signatureService = this.constructSignatureService();
    this.derivedKeySignatureService = this.constructDerivedKeySignatureService();
  }

  /**
   * Helper function to print log statements for Integration Tests which is network specific.
   *
   * @param transactionType {@link TransactionType} to be logged for the executed transaction.
   * @param hash            {@link Hash256} to be logged for the executed transaction.
   */
  protected void logInfo(TransactionType transactionType, Hash256 hash) {
    String url = System.getProperty("useTestnet") != null ? "https://testnet.xrpl.org/transactions/" :
      (System.getProperty("useDevnet") != null ? "https://devnet.xrpl.org/transactions/" : "");
    logger.info("{} transaction successful: {}{}", transactionType.value(), url, hash);
  }

  protected KeyPair createRandomAccountEd25519() {
    // Create the account
    final KeyPair randomKeyPair = Seed.ed25519Seed().deriveKeyPair();
    logAccountCreation(randomKeyPair.publicKey().deriveAddress());

    fundAccount(randomKeyPair.publicKey().deriveAddress());

    return randomKeyPair;
  }

  protected KeyPair createRandomAccountSecp256k1() {
    // Create the account
    final KeyPair randomKeyPair = Seed.secp256k1Seed().deriveKeyPair();
    logAccountCreation(randomKeyPair.publicKey().deriveAddress());

    fundAccount(randomKeyPair.publicKey().deriveAddress());

    return randomKeyPair;
  }

  protected PrivateKeyReference createRandomPrivateKeyReferenceEd25519() {
    final PrivateKeyReference privateKeyReference = new PrivateKeyReference() {
      @Override
      public KeyType keyType() {
        return KeyType.ED25519;
      }

      @Override
      public String keyIdentifier() {
        return UUID.randomUUID().toString();
      }
    };

    PublicKey publicKey = derivedKeySignatureService.derivePublicKey(privateKeyReference);
    logAccountCreation(publicKey.deriveAddress());

    fundAccount(publicKey.deriveAddress());

    return privateKeyReference;
  }

  protected PrivateKeyReference createRandomPrivateKeyReferenceSecp256k1() {
    final PrivateKeyReference privateKeyReference = new PrivateKeyReference() {
      @Override
      public KeyType keyType() {
        return KeyType.SECP256K1;
      }

      @Override
      public String keyIdentifier() {
        return UUID.randomUUID().toString();
      }
    };

    PublicKey publicKey = derivedKeySignatureService.derivePublicKey(privateKeyReference);
    logAccountCreation(publicKey.deriveAddress());

    fundAccount(publicKey.deriveAddress());

    return privateKeyReference;
  }

  /**
   * Funds a wallet with 1000 XRP.
   *
   * @param address The {@link Address} to fund.
   */
  protected void fundAccount(final Address address) {
    Objects.requireNonNull(address);
    xrplEnvironment.fundAccount(address);
  }

  protected <T extends Transaction> TransactionResult<T> signSubmitAndWait(
    T transaction,
    KeyPair keyPair,
    Class<T> transactionType
  )
    throws JsonRpcClientErrorException, JsonProcessingException {
    Preconditions.checkArgument(transaction.lastLedgerSequence().isPresent());

    SingleSignedTransaction<T> signedTransaction = signatureService.sign(
      keyPair.privateKey(),
      transaction
    );

    SubmitResult<T> voteSubmitResult = xrplClient.submit(signedTransaction);
    assertThat(voteSubmitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    Finality finality = scanForFinality(
      signedTransaction.hash(),
      voteSubmitResult.validatedLedgerIndex(),
      transaction.lastLedgerSequence().get(),
      transaction.sequence(),
      keyPair.publicKey().deriveAddress()
    );

    assertThat(finality.finalityStatus()).isEqualTo(FinalityStatus.VALIDATED_SUCCESS);

    return this.getValidatedTransaction(signedTransaction.hash(), transactionType);
  }

  //////////////////////
  // Ledger Helpers

  /// ///////////////////

  protected Finality scanForFinality(
    Hash256 transactionHash,
    LedgerIndex submittedOnLedgerIndex,
    UnsignedInteger lastLedgerSequence,
    UnsignedInteger transactionAccountSequence,
    Address account
  ) {
    return given()
      .pollInterval(POLL_INTERVAL)
      .atMost(AT_MOST_INTERVAL)
      .ignoreException(RuntimeException.class)
      .await()
      .until(
        () -> xrplClient.isFinal(
          transactionHash,
          submittedOnLedgerIndex,
          lastLedgerSequence,
          transactionAccountSequence,
          account
        ),
        is(equalTo(
            Finality.builder()
              .finalityStatus(FinalityStatus.VALIDATED_SUCCESS)
              .resultCode(TransactionResultCodes.TES_SUCCESS)
              .build()
          )
        )
      );
  }

  protected <T> T scanForResult(Supplier<T> resultSupplier, Predicate<T> condition) {
    return given()
      .atMost(AT_MOST_INTERVAL)
      .pollInterval(POLL_INTERVAL)
      .await()
      .until(() -> {
        T result = resultSupplier.get();
        if (result == null) {
          return null;
        }
        return condition.test(result) ? result : null;
      }, is(notNullValue()));
  }

  protected <T extends XrplResult> T scanForResult(Supplier<T> resultSupplier) {
    Objects.requireNonNull(resultSupplier);
    return given()
      .pollInterval(POLL_INTERVAL)
      .atMost(AT_MOST_INTERVAL)
      .ignoreException(RuntimeException.class)
      .await()
      .until(resultSupplier::get, is(notNullValue()));
  }

  protected <T extends LedgerObject> T scanForLedgerObject(Supplier<T> ledgerObjectSupplier) {
    Objects.requireNonNull(ledgerObjectSupplier);
    return given()
      .pollInterval(POLL_INTERVAL)
      .atMost(AT_MOST_INTERVAL)
      .ignoreException(RuntimeException.class)
      .await()
      .until(ledgerObjectSupplier::get, is(notNullValue()));
  }

  protected AccountObjectsResult getValidatedAccountObjects(Address classicAddress) {
    try {
      AccountObjectsRequestParams params = AccountObjectsRequestParams.builder()
        .account(classicAddress)
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build();
      return xrplClient.accountObjects(params);
    } catch (JsonRpcClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected <T extends LedgerObject> List<T> getValidatedAccountObjects(Address classicAddress, Class<T> clazz) {
    try {
      AccountObjectsRequestParams params = AccountObjectsRequestParams.builder()
        .account(classicAddress)
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build();
      List<LedgerObject> ledgerObjects = xrplClient.accountObjects(params).accountObjects();
      return ledgerObjects
        .stream()
        .filter(object -> clazz.isAssignableFrom(object.getClass()))
        .map(object -> (T) object)
        .collect(Collectors.toList());
    } catch (JsonRpcClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected AccountChannelsResult getValidatedAccountChannels(Address classicAddress) {
    try {
      AccountChannelsRequestParams params = AccountChannelsRequestParams.builder()
        .account(classicAddress)
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build();
      return xrplClient.accountChannels(params);
    } catch (JsonRpcClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected AccountInfoResult getValidatedAccountInfo(Address classicAddress) {
    try {
      AccountInfoRequestParams params = AccountInfoRequestParams.builder()
        .account(classicAddress)
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build();
      return xrplClient.accountInfo(params);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected <T extends Transaction> TransactionResult<T> getValidatedTransaction(
    Hash256 transactionHash,
    Class<T> transactionType
  ) {
    try {
      TransactionResult<T> transaction = xrplClient.transaction(
        TransactionRequestParams.of(transactionHash),
        transactionType
      );
      return transaction.validated() ? transaction : null;
    } catch (JsonRpcClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected LedgerResult getValidatedLedger() {
    try {
      LedgerRequestParams params = LedgerRequestParams.builder()
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build();
      return xrplClient.ledger(params);
    } catch (JsonRpcClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected RipplePathFindResult getValidatedRipplePath(
    KeyPair sourceKeyPair,
    KeyPair destinationKeyPair,
    IssuedCurrencyAmount destinationAmount
  ) {
    try {
      RipplePathFindRequestParams pathFindParams = RipplePathFindRequestParams.builder()
        .sourceAccount(sourceKeyPair.publicKey().deriveAddress())
        .destinationAccount(destinationKeyPair.publicKey().deriveAddress())
        .destinationAmount(destinationAmount)
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build();

      return xrplClient.ripplePathFind(pathFindParams);
    } catch (JsonRpcClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected RipplePathFindResult getValidatedRipplePath(
    KeyPair sourceKeyPair,
    KeyPair destinationKeyPair,
    IssuedCurrencyAmount destinationAmount,
    Hash256 domain
  ) {
    try {
      RipplePathFindRequestParams pathFindParams = RipplePathFindRequestParams.builder()
        .sourceAccount(sourceKeyPair.publicKey().deriveAddress())
        .destinationAccount(destinationKeyPair.publicKey().deriveAddress())
        .destinationAmount(destinationAmount)
        .domain(domain)
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build();

      return xrplClient.ripplePathFind(pathFindParams);
    } catch (JsonRpcClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected AccountLinesResult getValidatedAccountLines(Address classicAddress, Address peerAddress) {
    try {
      AccountLinesRequestParams params = AccountLinesRequestParams.builder()
        .account(classicAddress)
        .peer(peerAddress)
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build();

      return xrplClient.accountLines(params);
    } catch (JsonRpcClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected UnsignedLong instantToXrpTimestamp(Instant instant) {
    return UnsignedLong.valueOf(instant.getEpochSecond() - 0x386d4380);
  }

  protected Instant xrpTimestampToInstant(UnsignedLong xrpTimeStamp) {
    return Instant.ofEpochSecond(xrpTimeStamp.plus(UnsignedLong.valueOf(0x386d4380)).longValue());
  }

  /**
   * Create a trustline between the issuer of the specified {@param trustlineLimitAmount} and specified counterparty for
   * the given currency code with the given limit.
   *
   * @param counterpartyKeyPair  The {@link KeyPair} of the counterparty account.
   * @param trustlineLimitAmount A {@link IssuedCurrencyAmount} representing the trust limit for the counterparty.
   * @param fee                  The current network fee, as an {@link XrpCurrencyAmount}.
   *
   * @return The {@link TrustLine} that gets created.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   */
  public TrustLine createTrustLine(
    KeyPair counterpartyKeyPair,
    IssuedCurrencyAmount trustlineLimitAmount,
    XrpCurrencyAmount fee
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    return createTrustLine(
      counterpartyKeyPair, trustlineLimitAmount, fee, TrustSetFlags.builder().tfSetNoRipple().build()
    );
  }

  /**
   * Create a trustline between the issuer of the specified {@param trustlineLimitAmount} and specified counterparty for
   * the given currency code with the given limit.
   *
   * @param counterpartyKeyPair  The {@link KeyPair} of the counterparty account.
   * @param trustlineLimitAmount A {@link IssuedCurrencyAmount} representing the trust limit for the counterparty.
   * @param fee                  The current network fee, as an {@link XrpCurrencyAmount}.
   * @param trustSetFlags        A {@link TrustSetFlags} to use when creating the trustline.
   *
   * @return The {@link TrustLine} that gets created.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   */
  public TrustLine createTrustLine(
    KeyPair counterpartyKeyPair,
    IssuedCurrencyAmount trustlineLimitAmount,
    XrpCurrencyAmount fee,
    TrustSetFlags trustSetFlags
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    Address counterpartyAddress = counterpartyKeyPair.publicKey().deriveAddress();

    AccountInfoResult counterpartyAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(counterpartyAddress)
    );

    TrustSet trustSet = TrustSet.builder()
      .account(counterpartyAddress)
      .fee(fee)
      .sequence(counterpartyAccountInfo.accountData().sequence())
      .limitAmount(trustlineLimitAmount)
      .flags(trustSetFlags)
      .signingPublicKey(counterpartyKeyPair.publicKey())
      .build();

    SingleSignedTransaction<TrustSet> signedTrustSet = signatureService.sign(
      counterpartyKeyPair.privateKey(),
      trustSet
    );
    SubmitResult<TrustSet> trustSetSubmitResult = xrplClient.submit(signedTrustSet);
    assertThat(trustSetSubmitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    logInfo(
      trustSetSubmitResult.transactionResult().transaction().transactionType(),
      trustSetSubmitResult.transactionResult().hash()
    );

    return scanForResult(
      () -> getValidatedAccountLines(trustlineLimitAmount.issuer(), counterpartyAddress),
      linesResult -> !linesResult.lines().isEmpty()
    ).lines().get(0);
  }

  /**
   * Send issued currency funds from a sender to a receiver.
   *
   * @param senderKeyPair   The {@link KeyPair} of the payment sender.
   * @param receiverKeyPair The {@link KeyPair} of the payment receiver.
   * @param amount          An {@link IssuedCurrencyAmount} to send.
   * @param fee             The current network fee, as an {@link XrpCurrencyAmount}.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   */
  public void sendIssuedCurrency(
    KeyPair senderKeyPair,
    KeyPair receiverKeyPair,
    IssuedCurrencyAmount amount,
    XrpCurrencyAmount fee
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    sendIssuedCurrency(senderKeyPair, receiverKeyPair, amount, fee, TransactionResultCodes.TES_SUCCESS);
  }

  /**
   * Send issued currency funds from a sender to a receiver.
   *
   * @param senderKeyPair        The {@link KeyPair} of the payment sender.
   * @param receiverKeyPair      The {@link KeyPair} of the payment receiver.
   * @param amount               An {@link IssuedCurrencyAmount} to send.
   * @param fee                  The current network fee, as an {@link XrpCurrencyAmount}.
   * @param expectedEngineResult The expected engine result.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   */
  public void sendIssuedCurrency(
    KeyPair senderKeyPair,
    KeyPair receiverKeyPair,
    IssuedCurrencyAmount amount,
    XrpCurrencyAmount fee,
    String expectedEngineResult
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    Objects.requireNonNull(senderKeyPair);
    Objects.requireNonNull(receiverKeyPair);
    Objects.requireNonNull(amount);
    Objects.requireNonNull(fee);
    Objects.requireNonNull(expectedEngineResult);

    final Address senderAddress = senderKeyPair.publicKey().deriveAddress();
    final Address receiverAddress = receiverKeyPair.publicKey().deriveAddress();

    int loopGuard = 0;
    String paymentEngineResult = null;

    while (!expectedEngineResult.equalsIgnoreCase(paymentEngineResult)) {
      if (loopGuard++ > 30) {
        throw new RuntimeException(
          String.format("engineResult should have been `%s`, but was `%s` instead",
            expectedEngineResult, paymentEngineResult
          )
        );
      }

      ///////////////////////////
      // Sender sends a payment with the issued currency to the counterparty
      AccountInfoResult senderAccountInfo = this.scanForResult(
        () -> getValidatedAccountInfo(senderAddress)
      );
      UnsignedInteger currentSenderSequence = senderAccountInfo.accountData().sequence();
      logger.info("About to send a payment on Sequence={}", currentSenderSequence);

      Payment fundCounterparty = Payment.builder()
        .account(senderAddress)
        .fee(fee)
        .sequence(senderAccountInfo.accountData().sequence())
        .destination(receiverAddress)
        .amount(amount)
        .signingPublicKey(senderKeyPair.publicKey())
        .build();

      SingleSignedTransaction<Payment> signedPayment = signatureService.sign(
        senderKeyPair.privateKey(), fundCounterparty
      );
      SubmitResult<Payment> paymentResult = xrplClient.submit(signedPayment);
      assertThat(paymentResult.transactionResult().hash()).isEqualTo(signedPayment.hash());

      paymentEngineResult = paymentResult.engineResult();
      if (!paymentResult.engineResult().equals(expectedEngineResult)) {
        try {
          // If the code gets here, it means the transaction did not succeed. The most typical reason here is a latent
          // Clio node (see description at the end of this function). This loop allows the code to retry for just a bit
          // longer than the current 60s DNS TTL.
          Thread.sleep(3000); // <-- Sleep for 3 seconds and try again
        } catch (InterruptedException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
        logger.error(
          "PaymentEngineResult `{}` did not equal expectedEngineResult `{}`", paymentEngineResult, expectedEngineResult
        );
        continue; // <-- Try again, up to the loop guard above.
      }

      logInfo(
        paymentResult.transactionResult().transaction().transactionType(),
        paymentResult.transactionResult().hash()
      );

      this.scanForResult(
        () -> getValidatedTransaction(
          paymentResult.transactionResult().hash(),
          Payment.class)
      );

      // This extra check exists for Clio servers. Occasionally, one Clio server in the cluster will report that a TX
      // (e.g., with sequence 5) is `VALIDATED`. Subsequent calls to `account_info` should return an account sequence
      // number of 6, but sometimes one of the servers in the cluster will return the old sequence value of 5. This
      // scanner simply waits until at least one of the Clio server reports the correct account_sequence number so that
      // subsequent calls to `account_info` will typically have the correct account sequence. Note that this solution
      // will _mostly_ work, but not always.  Consider an example with three Clio nodes in a cluster. Node A is latent,
      // but B & C are not (i.e., B & C have an up-to-date account sequence). In this instance, this scanner might
      // receive a result from B or C, but on the next payment, this code might get a response from the (incorrect)
      // node A. In reality, this should _almost_ never happen because the current testnet DNS configuration
      // pegs JSON-RPC clients to a single IP address (i.e., single clio server) for 60s. Therefore, there should
      // only be very tiny windows where this solution does not fix the issue. For that, we have the loop above as
      // well to retry.
      this.scanForResult(
        () -> getValidatedAccountInfo(senderAddress),
        result -> result.accountData().sequence().equals(
          senderAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE)
        )
      );
    }
  }

  //////////////////
  // Private Helpers

  /// ///////////////

  protected PrivateKeyReference constructPrivateKeyReference(
    final String keyIdentifier, final KeyType keyType
  ) {
    Objects.requireNonNull(keyIdentifier);
    Objects.requireNonNull(keyType);

    return new PrivateKeyReference() {
      @Override
      public String keyIdentifier() {
        return keyIdentifier;
      }

      @Override
      public KeyType keyType() {
        return keyType;
      }
    };
  }

  protected PrivateKey constructPrivateKey(
    final String keyIdentifier, final KeyType keyType
  ) {
    Objects.requireNonNull(keyIdentifier);
    Objects.requireNonNull(keyType);

    switch (keyType) {
      case ED25519: {
        return Seed.ed25519Seed().deriveKeyPair().privateKey();
      }
      case SECP256K1: {
        return Seed.secp256k1Seed().deriveKeyPair().privateKey();
      }
      default: {
        throw new RuntimeException("Unhandled KeyType: " + keyType);
      }
    }
  }

  protected SignatureService<PrivateKeyReference> constructDerivedKeySignatureService() {
    try {
      final Key secretKey = loadKeyStore().getKey("secret0", "password".toCharArray());
      return new BcDerivedKeySignatureService(() -> ServerSecret.of(secretKey.getEncoded()));
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected SignatureService<PrivateKey> constructSignatureService() {
    return new BcSignatureService();
  }

  protected KeyPair constructRandomAccount() {
    // Create the account
    final KeyPair randomKeyPair = Seed.ed25519Seed().deriveKeyPair();
    logAccountCreation(randomKeyPair.publicKey().deriveAddress());

    fundAccount(randomKeyPair.publicKey().deriveAddress());

    return randomKeyPair;
  }


  protected PublicKey toPublicKey(final PrivateKeyReference privateKeyReference) {
    return derivedKeySignatureService.derivePublicKey(privateKeyReference);
  }

  protected Address toAddress(final PrivateKeyReference privateKeyReference) {
    return toPublicKey(privateKeyReference).deriveAddress();
  }

  private KeyStore loadKeyStore() {
    final String jksFileName = "crypto/crypto.p12";
    final char[] jksPassword = "password".toCharArray();
    return JavaKeystoreLoader.loadFromClasspath(jksFileName, jksPassword);
  }

  /**
   * Returns the minimum time that can be used for escrow expirations. The ledger will not accept an expiration time
   * that is earlier than the last ledger close time, so we must use the latter of current time or ledger close time
   * (which for unexplained reasons can sometimes be later than now).
   *
   * @return An {@link Instant}.
   */
  protected Instant getMinExpirationTime() {
    LedgerResult result = getValidatedLedger();
    Instant closeTime = xrpTimestampToInstant(
      result.ledger().closeTime()
        .orElseThrow(() ->
          new RuntimeException("Ledger close time must be present to calculate a minimum expiration time.")
        )
    );

    Instant now = Instant.now();
    return closeTime.isBefore(now) ? now : closeTime;
  }

  private void logAccountCreation(Address address) {
    logger.info("Generated wallet with ClassicAddress={})", address);
  }

  protected void createAndAcceptCredentials(
    KeyPair isserKeyPair, KeyPair subjectKeyPair, CredentialType[] credentialTypes
  ) throws JsonRpcClientErrorException, JsonProcessingException {

    createCredentials(isserKeyPair, subjectKeyPair, credentialTypes);

    acceptCredentials(isserKeyPair, subjectKeyPair, credentialTypes);
  }

  protected void createCredentials(KeyPair issuerKeyPair, KeyPair subjectKeyPair, CredentialType[] credentialTypes)
    throws JsonRpcClientErrorException, JsonProcessingException {

    for (CredentialType credentialType : credentialTypes) {
      FeeResult feeResult = xrplClient.fee();
      AccountInfoResult issuerAccountInfo = this.scanForResult(
        () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
      );

      CredentialCreate credCreateTx = CredentialCreate.builder()
        .account(issuerKeyPair.publicKey().deriveAddress())
        .sequence(issuerAccountInfo.accountData().sequence())
        .fee(feeResult.drops().openLedgerFee())
        .subject(subjectKeyPair.publicKey().deriveAddress())
        .credentialType(credentialType)
        .signingPublicKey(issuerKeyPair.publicKey())
        .build();

      SingleSignedTransaction<CredentialCreate> signedCreateTx = signatureService.sign(
        issuerKeyPair.privateKey(), credCreateTx
      );

      SubmitResult<CredentialCreate> createTxIntermediateResult = xrplClient.submit(signedCreateTx);

      assertThat(createTxIntermediateResult.engineResult()).isEqualTo("tesSUCCESS");

      // Then wait until the transaction gets committed to a validated ledger
      this.scanForResult(
        () ->
          this.getValidatedTransaction(createTxIntermediateResult.transactionResult().hash(), CredentialCreate.class)
      );
    }
  }

  protected void createPermissionedDomain(KeyPair domainOwnerKeyPair, KeyPair credentialIssuerKeyPair,
    CredentialType[] credentialTypes)
    throws JsonRpcClientErrorException, JsonProcessingException {

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult domainOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(domainOwnerKeyPair.publicKey().deriveAddress())
    );
    UnsignedInteger createSequence = domainOwnerAccountInfo.accountData().sequence();

    // Create a PermissionedDomain object.
    List<CredentialWrapper> credentials = Arrays.stream(credentialTypes)
      .map(credentialType -> CredentialWrapper.builder()
        .credential(Credential.builder()
          .issuer(credentialIssuerKeyPair.publicKey().deriveAddress())
          .credentialType(credentialType)
          .build())
        .build())
      .collect(Collectors.toList());

    PermissionedDomainSet permissionedDomainSetTx = PermissionedDomainSet.builder()
      .account(domainOwnerKeyPair.publicKey().deriveAddress())
      .sequence(createSequence)
      .fee(feeResult.drops().openLedgerFee())
      .acceptedCredentials(credentials)
      .signingPublicKey(domainOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<PermissionedDomainSet> signedCreateTx = signatureService.sign(
      domainOwnerKeyPair.privateKey(), permissionedDomainSetTx
    );

    SubmitResult<PermissionedDomainSet> domainSetTxIntermediateResult = xrplClient.submit(signedCreateTx);

    assertThat(domainSetTxIntermediateResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(
        domainSetTxIntermediateResult.transactionResult().hash(), PermissionedDomainSet.class)
    );
  }

  protected void acceptCredentials(KeyPair issuerKeyPair, KeyPair subjectKeyPair, CredentialType[] credentialTypes)
    throws JsonRpcClientErrorException, JsonProcessingException {

    for (CredentialType credentialType : credentialTypes) {
      FeeResult feeResult = xrplClient.fee();
      AccountInfoResult subjectAccountInfo = this.scanForResult(
        () -> this.getValidatedAccountInfo(subjectKeyPair.publicKey().deriveAddress())
      );

      // Accept Credential
      CredentialAccept credAcceptTx = CredentialAccept.builder()
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .account(subjectKeyPair.publicKey().deriveAddress())
        .credentialType(credentialType)
        .sequence(subjectAccountInfo.accountData().sequence())
        .fee(feeResult.drops().openLedgerFee())
        .signingPublicKey(subjectKeyPair.publicKey())
        .build();

      SingleSignedTransaction<CredentialAccept> signedAcceptTx = signatureService.sign(
        subjectKeyPair.privateKey(), credAcceptTx
      );

      SubmitResult<CredentialAccept> acceptTxIntermediateResult = xrplClient.submit(signedAcceptTx);

      assertThat(acceptTxIntermediateResult.engineResult()).isEqualTo("tesSUCCESS");

      // Then wait until the transaction gets committed to a validated ledger
      this.scanForResult(
        () ->
          this.getValidatedTransaction(acceptTxIntermediateResult.transactionResult().hash(), CredentialAccept.class)
      );
    }
  }

  /**
   * Enable the lsfDepositAuth flag on a given account by submitting an {@link AccountSet} transaction.
   *
   * @param wallet The {@link KeyPair} of the account to enable Deposit Authorization on.
   *
   * @return The {@link AccountInfoResult} of the wallet once the {@link AccountSet} transaction has been applied.
   *
   * @throws JsonRpcClientErrorException If {@code xrplClient} throws an error.
   */
  protected AccountInfoResult enableDepositAuthorization(KeyPair wallet)
    throws JsonRpcClientErrorException, JsonProcessingException {

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.publicKey().deriveAddress())
    );
    AccountSet accountSet = AccountSet.builder()
      .account(wallet.publicKey().deriveAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(accountInfoResult.accountData().sequence())
      .signingPublicKey(wallet.publicKey())
      .setFlag(AccountSet.AccountSetFlag.DEPOSIT_AUTH)
      .build();

    SingleSignedTransaction<AccountSet> signedAccountSet = signatureService.sign(
      wallet.privateKey(), accountSet
    );
    SubmitResult<AccountSet> accountSetResult = xrplClient.submit(signedAccountSet);
    assertThat(accountSetResult.engineResult()).isEqualTo("tesSUCCESS");
    logger.info(
      "AccountSet to enable Deposit Authorization successful. https://testnet.xrpl.org/transactions/{}",
      accountSetResult.transactionResult().hash()
    );
    return this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.publicKey().deriveAddress()),
      accountInfo -> accountInfo.accountData().flags().lsfDepositAuth()
    );
  }

  protected void preAuthorizeCredentials(KeyPair issuerKeyPair, KeyPair receiverKeyPair,
    CredentialType[] credentialTypes) throws JsonRpcClientErrorException, JsonProcessingException {

    List<CredentialWrapper> credsToAuthorize = Arrays.stream(credentialTypes).map(
      credentialType -> CredentialWrapper.builder()
        .credential(Credential.builder()
          .credentialType(credentialType)
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .build()
        ).build()).collect(Collectors.toList());

    FeeResult feeResult = xrplClient.fee();

    AccountInfoResult receiverAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverKeyPair.publicKey().deriveAddress())
    );

    DepositPreAuth depositPreAuthTx = DepositPreAuth.builder()
      .account(receiverKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(receiverAccountInfo.accountData().sequence())
      .signingPublicKey(receiverKeyPair.publicKey())
      .authorizeCredentials(credsToAuthorize)
      .build();

    SingleSignedTransaction<DepositPreAuth> singedDepositPreAuth = this.signatureService.sign(
      receiverKeyPair.privateKey(), depositPreAuthTx
    );

    SubmitResult<DepositPreAuth> depositPreAuthSubmitResult = xrplClient.submit(singedDepositPreAuth);

    assertThat(depositPreAuthSubmitResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(depositPreAuthSubmitResult.transactionResult().hash(), CredentialAccept.class)
    );
  }

  protected List<Hash256> getCredentialObjectIds(KeyPair issuerKeyPair, KeyPair subjectKeyPair,
    CredentialType[] credentialTypes) {

    return Arrays.stream(credentialTypes).map(credentialType -> {
        try {
          return xrplClient.ledgerEntry(
            LedgerEntryRequestParams.credential(
              CredentialLedgerEntryParams.builder()
                .issuer(issuerKeyPair.publicKey().deriveAddress())
                .subject(subjectKeyPair.publicKey().deriveAddress())
                .credentialType(credentialType)
                .build(),
              LedgerSpecifier.VALIDATED
            )
          );
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      }
    ).map(LedgerEntryResult::index).collect(Collectors.toList());
  }

  protected PermissionedDomainObject getPermissionedDomainObject(Address domainOwner) {
    return (PermissionedDomainObject) this.scanForResult(
      () -> {
        try {
          return xrplClient.accountObjects(AccountObjectsRequestParams.builder()
            .type(AccountObjectType.PERMISSIONED_DOMAIN)
            .account(domainOwner)
            .ledgerSpecifier(LedgerSpecifier.VALIDATED)
            .build()
          ).accountObjects();
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> result.size() == 1
    ).get(0);
  }

  /**
   * Set the {@code lsfDefaultRipple} flag on an issuer account.
   *
   * @param issuerKeyPair The {@link KeyPair} containing the address of the issuer account.
   * @param feeResult     The current {@link FeeResult}.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   */
  protected void setDefaultRipple(KeyPair issuerKeyPair, FeeResult feeResult)
    throws JsonRpcClientErrorException, JsonProcessingException {
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    AccountSet setDefaultRipple = AccountSet.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .signingPublicKey(issuerKeyPair.publicKey())
      .setFlag(AccountSet.AccountSetFlag.DEFAULT_RIPPLE)
      .build();

    SingleSignedTransaction<AccountSet> signedAccountSet = signatureService.sign(
      issuerKeyPair.privateKey(), setDefaultRipple
    );
    SubmitResult<AccountSet> setResult = xrplClient.submit(signedAccountSet);
    assertThat(setResult.engineResult()).isEqualTo("tesSUCCESS");
    logger.info(
      "AccountSet transaction successful: https://testnet.xrpl.org/transactions/{}",
      setResult.transactionResult().hash()
    );

    scanForResult(
      () -> getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress()),
      info -> info.accountData().flags().lsfDefaultRipple()
    );
  }
}
