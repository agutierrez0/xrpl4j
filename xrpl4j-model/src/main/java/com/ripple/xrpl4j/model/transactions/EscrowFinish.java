package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.cryptoconditions.Condition;
import com.ripple.cryptoconditions.Fulfillment;

import com.ripple.xrpl4j.model.transactions.immutables.FluentCompareTo;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * Deliver XRP from a held payment to the recipient.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableEscrowFinish.class)
@JsonDeserialize(as = ImmutableEscrowFinish.class)
public interface EscrowFinish extends Transaction<Flags.TransactionFlags> {

  static ImmutableEscrowFinish.Builder builder() {
    return ImmutableEscrowFinish.builder();
  }

  /**
   * Compute the fee for the supplied {@code fulfillment}. The minimum transaction cost to submit an EscrowFinish
   * transaction increases if it contains a fulfillment. If the transaction contains a fulfillment, the transaction cost
   * is 330 drops of XRP plus another 10 drops for every 16 bytes in size of the preimage.
   *
   * @param currentLedgerFeeDrops The number of drops that the ledger demands at present.
   * @param fulfillment           The {@link Fulfillment} that is being presented to the ledger for computation
   *                              purposes.
   * @return
   * @see "https://xrpl.org/escrowfinish.html"
   */
  static XrpCurrencyAmount computeFee(final XrpCurrencyAmount currentLedgerFeeDrops, final Fulfillment fulfillment) {
    Objects.requireNonNull(currentLedgerFeeDrops);
    Objects.requireNonNull(fulfillment);

    BigInteger newFee = (
      currentLedgerFeeDrops.asBigInteger().add( // <-- usually 10 drops, per the docs.
        BigInteger.valueOf(320))
        .add( // <-- https://github.com/ripple/rippled/blob/develop/src/ripple/app/tx/impl/Escrow.cpp#L362
          BigInteger.valueOf(
            10 * (fulfillment.getDerivedCondition().getCost() / 16))) // <-- 10 drops for each additional 16 bytes.
    );
    return XrpCurrencyAmount.of(newFee.toString());
  }

  @JsonProperty("Flags")
  @Value.Derived
  default Flags.TransactionFlags flags() {
    return new Flags.TransactionFlags.Builder().fullyCanonicalSig(true).build();
  }

  /**
   * {@link Address} of the source account that funded the escrow payment.
   */
  @JsonProperty("Owner")
  Address owner();

  /**
   * The {@link EscrowCreate#sequence()} of the transaction that created the escrow to cancel.
   */
  @JsonProperty("OfferSequence")
  UnsignedInteger offerSequence();

  /**
   * Hex value matching the previously-supplied PREIMAGE-SHA-256 crypto-condition of the held payment.
   */
  @JsonProperty("Condition")
  Optional<Condition> condition();

  /**
   * Hex value of the PREIMAGE-SHA-256 crypto-condition fulfillment matching the held payment's {@code condition}.
   */
  @JsonProperty("Fulfillment")
  Optional<Fulfillment> fulfillment();

  @Value.Check
  default void check() {
    fulfillment().ifPresent(f -> {
        BigInteger feeInDrops = fee().asBigInteger();
        Preconditions.checkState(condition().isPresent(),
          "If a fulfillment is specified, the corresponding condition must also be specified.");
        Preconditions.checkState(FluentCompareTo.is(feeInDrops).greaterThanEqualTo(BigInteger.valueOf(330)),
          "If a fulfillment is specified, the fee must be set to 330 or greater.");
      }
    );
    condition().ifPresent($ -> Preconditions.checkState(fulfillment().isPresent(),
      "If a condition is specified, the corresponding fulfillment must also be specified."));
  }

}