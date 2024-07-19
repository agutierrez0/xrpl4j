package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
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

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.immutables.FluentCompareTo;
import org.xrpl.xrpl4j.model.immutables.Wrapped;
import org.xrpl.xrpl4j.model.immutables.Wrapper;
import org.xrpl.xrpl4j.model.jackson.modules.AddressDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.AddressSerializer;
import org.xrpl.xrpl4j.model.jackson.modules.AssetPriceDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.AssetPriceSerializer;
import org.xrpl.xrpl4j.model.jackson.modules.DidDataDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.DidDataSerializer;
import org.xrpl.xrpl4j.model.jackson.modules.DidDocumentDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.DidDocumentSerializer;
import org.xrpl.xrpl4j.model.jackson.modules.DidUriDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.DidUriSerializer;
import org.xrpl.xrpl4j.model.jackson.modules.Hash256Deserializer;
import org.xrpl.xrpl4j.model.jackson.modules.Hash256Serializer;
import org.xrpl.xrpl4j.model.jackson.modules.MarkerDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.MarkerSerializer;
import org.xrpl.xrpl4j.model.jackson.modules.NetworkIdDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.NetworkIdSerializer;
import org.xrpl.xrpl4j.model.jackson.modules.NfTokenIdDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.NfTokenIdSerializer;
import org.xrpl.xrpl4j.model.jackson.modules.NfTokenUriSerializer;
import org.xrpl.xrpl4j.model.jackson.modules.OracleDocumentIdDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.OracleDocumentIdSerializer;
import org.xrpl.xrpl4j.model.jackson.modules.OracleProviderDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.OracleUriDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.TradingFeeDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.TradingFeeSerializer;
import org.xrpl.xrpl4j.model.jackson.modules.TransferFeeDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.TransferFeeSerializer;
import org.xrpl.xrpl4j.model.jackson.modules.VoteWeightDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.VoteWeightSerializer;
import org.xrpl.xrpl4j.model.jackson.modules.XChainClaimIdDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.XChainClaimIdSerializer;
import org.xrpl.xrpl4j.model.jackson.modules.XChainCountDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.XChainCountSerializer;
import org.xrpl.xrpl4j.model.jackson.modules.XrpCurrencyAmountDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.XrpCurrencyAmountSerializer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * Wrapped immutable classes for providing type-safe objects.
 */
@SuppressWarnings("TypeName")
public class Wrappers {

  /**
   * A wrapped {@link String} representing an address on the XRPL.
   */
  @Value.Immutable(builder = true) // This is the default, but it's omitted without this.
  @Wrapped
  @JsonSerialize(as = Address.class, using = AddressSerializer.class)
  @JsonDeserialize(as = Address.class, using = AddressDeserializer.class)
  abstract static class _Address extends Wrapper<String> implements Serializable {

    @Override
    public String toString() {
      return this.value();
    }

    /**
     * Validates that a {@link Address}'s value's length is equal to 34 characters and starts with `r`.
     */
    @Value.Check
    public void validateAddress() {
      Preconditions.checkArgument(this.value().startsWith("r"), "Invalid Address: Bad Prefix");
      Preconditions.checkArgument(this.value().length() >= 25 && this.value().length() <= 35,
        "Classic Addresses must be (25,35) characters long inclusive.");
    }

  }

  /**
   * A wrapped {@link String} representing an X-Address on the XRPL.
   */
  @Value.Immutable(builder = true) // This is the default, but it's omitted without this.
  @Wrapped
  @JsonSerialize(as = XAddress.class)
  @JsonDeserialize(as = XAddress.class)
  abstract static class _XAddress extends Wrapper<String> implements Serializable {

    @Override
    public String toString() {
      return this.value();
    }

  }

  /**
   * A wrapped {@link String} containing the Hex representation of a 256-bit Hash.
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = Hash256.class, using = Hash256Serializer.class)
  @JsonDeserialize(as = Hash256.class, using = Hash256Deserializer.class)
  abstract static class _Hash256 extends Wrapper<String> implements Serializable {

    @Override
    public String toString() {
      return this.value();
    }

    /**
     * Validates that a {@link Hash256}'s value's length is equal to 64 characters.
     */
    @Value.Check
    public void validateLength() {
      Preconditions.checkArgument(this.value().length() == 64, "Hash256 Strings must be 64 characters long.");
    }

    @Override
    public boolean equals(Object obj) {
      if (obj != null && obj instanceof Hash256) {
        String otherValue = ((Hash256) obj).value();
        if (otherValue != null) {
          return otherValue.toUpperCase(Locale.ENGLISH).equals(value().toUpperCase(Locale.ENGLISH));
        }
      }
      return false;
    }

    @Override
    public int hashCode() {
      return value().toUpperCase(Locale.ENGLISH).hashCode();
    }
  }

  /**
   * A {@link CurrencyAmount} for the XRP currency (non-issued). {@link XrpCurrencyAmount}s are a {@link String}
   * representation of an unsigned integer representing the amount in XRP drops.
   */
  @Value.Immutable(builder = true) // This is the default, but it's omitted without this.
  @Wrapped
  @JsonSerialize(as = XrpCurrencyAmount.class, using = XrpCurrencyAmountSerializer.class)
  @JsonDeserialize(as = XrpCurrencyAmount.class, using = XrpCurrencyAmountDeserializer.class)
  abstract static class _XrpCurrencyAmount extends Wrapper<UnsignedLong> implements Serializable, CurrencyAmount {

    static final BigDecimal SMALLEST_XRP = new BigDecimal("0.000001");
    static final DecimalFormat FORMATTER = new DecimalFormat("###,###");

    /**
     * Constructs an {@link XrpCurrencyAmount} using a number of drops.
     *
     * @param drops A long representing the number of drops of XRP of this amount.
     *
     * @return An {@link XrpCurrencyAmount} of {@code drops}.
     */
    public static XrpCurrencyAmount ofDrops(long drops) {
      return ofDrops(UnsignedLong.valueOf(drops));
    }

    /**
     * Constructs an {@link XrpCurrencyAmount} using a number of drops.
     *
     * @param drops An {@link UnsignedLong} representing the number of drops of XRP of this amount.
     *
     * @return An {@link XrpCurrencyAmount} of {@code drops}.
     */
    public static XrpCurrencyAmount ofDrops(UnsignedLong drops) {
      return XrpCurrencyAmount.of(drops);
    }

    /**
     * Constructs an {@link XrpCurrencyAmount} using decimal amount of XRP.
     *
     * @param amount A {@link BigDecimal} amount of XRP.
     *
     * @return An {@link XrpCurrencyAmount} of the amount of drops in {@code amount}.
     */
    public static XrpCurrencyAmount ofXrp(BigDecimal amount) {
      if (FluentCompareTo.is(amount).notEqualTo(BigDecimal.ZERO)) {
        Preconditions.checkArgument(FluentCompareTo.is(amount).greaterThanEqualTo(SMALLEST_XRP));
      }
      return ofDrops(UnsignedLong.valueOf(amount.scaleByPowerOfTen(6).toBigIntegerExact()));
    }

    /**
     * Convert this XRP amount into a decimal representing a value denominated in whole XRP units. For example, a value
     * of `1.0` represents 1 unit of XRP; a value of `0.5` represents a half of an XRP unit.
     *
     * @return A {@link BigDecimal} representing this value denominated in whole XRP units.
     */
    public BigDecimal toXrp() {
      return new BigDecimal(this.value().bigIntegerValue())
        .divide(BigDecimal.valueOf(ONE_XRP_IN_DROPS), MathContext.DECIMAL128);
    }

    /**
     * Adds another {@link XrpCurrencyAmount} to this amount.
     *
     * @param other An {@link XrpCurrencyAmount} to add to this.
     *
     * @return The sum of this amount and the {@code other} amount, as an {@link XrpCurrencyAmount}.
     */
    public XrpCurrencyAmount plus(XrpCurrencyAmount other) {
      return XrpCurrencyAmount.of(this.value().plus(other.value()));
    }

    /**
     * Subtract another {@link XrpCurrencyAmount} from this amount.
     *
     * @param other An {@link XrpCurrencyAmount} to subtract from this.
     *
     * @return The difference of this amount and the {@code other} amount, as an {@link XrpCurrencyAmount}.
     */
    public XrpCurrencyAmount minus(XrpCurrencyAmount other) {
      return XrpCurrencyAmount.of(this.value().minus(other.value()));
    }

    /**
     * Multiplies this amount by another {@link XrpCurrencyAmount}.
     *
     * @param other An {@link XrpCurrencyAmount} to multiply to this by.
     *
     * @return The product of this amount and the {@code other} amount, as an {@link XrpCurrencyAmount}.
     */
    public XrpCurrencyAmount times(XrpCurrencyAmount other) {
      return XrpCurrencyAmount.of(this.value().times(other.value()));
    }

    @Override
    public String toString() {
      return this.value().toString();
    }

    /**
     * Validates that this {@link XrpCurrencyAmount} does not exceed the maximum number of drops.
     */
    @Value.Check
    protected void check() {
      Preconditions.checkState(
        FluentCompareTo.is(value()).lessThanOrEqualTo(UnsignedLong.valueOf(MAX_XRP_IN_DROPS)),
        String.format(
          "XRP Amounts may not exceed %s drops (100B XRP, denominated in Drops)", FORMATTER.format(MAX_XRP_IN_DROPS)
        )
      );
    }

  }

  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = Marker.class, using = MarkerSerializer.class)
  @JsonDeserialize(as = Marker.class, using = MarkerDeserializer.class)
  abstract static class _Marker extends Wrapper<String> implements Serializable {

    @Override
    @JsonRawValue
    public String toString() {
      return this.value();
    }

  }

  /**
   * A wrapped {@link String} containing the NFT Id.
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = NfTokenId.class, using = NfTokenIdSerializer.class)
  @JsonDeserialize(as = NfTokenId.class, using = NfTokenIdDeserializer.class)
  abstract static class _NfTokenId extends Wrapper<String> implements Serializable {

    @Override
    public String toString() {
      return this.value();
    }

    /**
     * Validates that a NfTokenId value's length is equal to 64 characters.
     */
    @Value.Check
    public void validateLength() {
      Preconditions.checkArgument(this.value().length() == 64, "TokenId must be 64 characters long.");
    }

    @Override
    public boolean equals(Object obj) {
      if (obj != null && obj instanceof NfTokenId) {
        String otherValue = ((NfTokenId) obj).value();
        if (otherValue != null) {
          return otherValue.toUpperCase(Locale.ENGLISH).equals(value().toUpperCase(Locale.ENGLISH));
        }
      }
      return false;
    }
  }

  /**
   * A wrapped {@link String} containing the Uri.
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = NfTokenUri.class, using = NfTokenUriSerializer.class)
  @JsonDeserialize(as = NfTokenUri.class)
  abstract static class _NfTokenUri extends Wrapper<String> implements Serializable {

    /**
     * Constructs an {@link NfTokenUri} using a String value.
     *
     * @param plaintext A string value representing the Uri in plaintext.
     *
     * @return An {@link NfTokenUri} of plaintext.
     */
    public static NfTokenUri ofPlainText(String plaintext) {
      return NfTokenUri.of(BaseEncoding.base16().encode(plaintext.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public boolean equals(Object obj) {
      if (obj != null && obj instanceof NfTokenUri) {
        String otherValue = ((NfTokenUri) obj).value();
        if (otherValue != null) {
          return otherValue.toUpperCase(Locale.ENGLISH).equals(value().toUpperCase(Locale.ENGLISH));
        }
      }
      return false;
    }
  }

  /**
   * A wrapped {@link com.google.common.primitives.UnsignedInteger} containing the TransferFee.
   *
   * <p>Valid values for this field are between 0 and 50000 inclusive, allowing transfer rates of between 0.00% and
   * 50.00% in increments of 0.001. If this field is provided in a {@link NfTokenMint} transaction, the transaction MUST
   * have the {@code tfTransferable} flag enabled.
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = TransferFee.class, using = TransferFeeSerializer.class)
  @JsonDeserialize(as = TransferFee.class, using = TransferFeeDeserializer.class)
  abstract static class _TransferFee extends Wrapper<UnsignedInteger> implements Serializable {

    @Override
    public String toString() {
      return this.value().toString();
    }

    /**
     * Construct {@link TransferFee} as a percentage value.
     *
     * <p>The given percentage value must have at most 3 decimal places of precision, and must be
     * between {@code 0} and {@code 50.000}.</p>
     *
     * @param percent of type {@link BigDecimal}
     *
     * @return {@link TransferFee}
     */
    public static TransferFee ofPercent(BigDecimal percent) {
      Objects.requireNonNull(percent);
      Preconditions.checkArgument(
        Math.max(0, percent.stripTrailingZeros().scale()) <= 3,
        "Percent value should have a maximum of 3 decimal places."
      );
      return TransferFee.of(UnsignedInteger.valueOf(percent.scaleByPowerOfTen(3).toBigIntegerExact()));
    }


    /**
     * Validates that a NfTokenId value's length is equal to 64 characters.
     */
    @Value.Check
    public void validateBounds() {
      Preconditions.checkArgument(
        FluentCompareTo.is(value()).lessThanOrEqualTo(UnsignedInteger.valueOf(50000)) &&
          FluentCompareTo.is(value()).greaterThanEqualTo(UnsignedInteger.valueOf(0)),
        "TransferFee should be in the range 0 to 50000.");
    }

  }

  /**
   * A wrapped {@link com.google.common.primitives.UnsignedInteger} containing a Network ID.
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = NetworkId.class, using = NetworkIdSerializer.class)
  @JsonDeserialize(as = NetworkId.class, using = NetworkIdDeserializer.class)
  abstract static class _NetworkId extends Wrapper<UnsignedInteger> implements Serializable {

    @Override
    public String toString() {
      return this.value().toString();
    }

    /**
     * Construct a {@link NetworkId} from a {@code long}. The supplied value must be less than or equal to
     * 4,294,967,295, the largest unsigned 32-bit integer.
     *
     * @param networkId A {@code long}.
     *
     * @return A {@link NetworkId}.
     */
    public static NetworkId of(long networkId) {
      return NetworkId.of(UnsignedInteger.valueOf(networkId));
    }
  }

  /**
   * A wrapped {@link com.google.common.primitives.UnsignedInteger} containing the TransferFee.
   *
   * <p>This class will be marked {@link com.google.common.annotations.Beta} until the AMM amendment is enabled on
   * mainnet. Its API is subject to change.</p>
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = TradingFee.class, using = TradingFeeSerializer.class)
  @JsonDeserialize(as = TradingFee.class, using = TradingFeeDeserializer.class)
  @Beta
  abstract static class _TradingFee extends Wrapper<UnsignedInteger> implements Serializable {

    @Override
    public String toString() {
      return this.value().toString();
    }

    /**
     * Construct {@link TradingFee} as a percentage value.
     *
     * @param percent The trading fee, as a {@link BigDecimal}.
     *
     * @return A {@link TradingFee}.
     */
    public static TradingFee ofPercent(BigDecimal percent) {
      Preconditions.checkArgument(
        Math.max(0, percent.stripTrailingZeros().scale()) <= 3,
        "Percent value should have a maximum of 3 decimal places."
      );
      return TradingFee.of(UnsignedInteger.valueOf(percent.scaleByPowerOfTen(3).toBigIntegerExact()));
    }

    /**
     * Get the {@link TradingFee} as a {@link BigDecimal}.
     *
     * @return A {@link BigDecimal}.
     */
    public BigDecimal bigDecimalValue() {
      return BigDecimal.valueOf(value().longValue(), 3);
    }

  }

  /**
   * A wrapped {@link com.google.common.primitives.UnsignedInteger} containing the VoteWeight.
   *
   * <p>This class will be marked {@link com.google.common.annotations.Beta} until the AMM amendment is enabled on
   * mainnet. Its API is subject to change.</p>
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = VoteWeight.class, using = VoteWeightSerializer.class)
  @JsonDeserialize(as = VoteWeight.class, using = VoteWeightDeserializer.class)
  @Beta
  abstract static class _VoteWeight extends Wrapper<UnsignedInteger> implements Serializable {

    @Override
    public String toString() {
      return this.value().toString();
    }

    /**
     * Get the {@link VoteWeight} as a {@link BigDecimal}.
     *
     * @return A {@link BigDecimal}.
     */
    public BigDecimal bigDecimalValue() {
      return BigDecimal.valueOf(value().longValue(), 3);
    }

  }

  /**
   * A wrapped {@link com.google.common.primitives.UnsignedLong} containing an XChainClaimID.
   *
   * <p>This class will be marked {@link com.google.common.annotations.Beta} until the featureXChainBridge amendment is
   * enabled on mainnet. Its API is subject to change.</p>
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = XChainClaimId.class, using = XChainClaimIdSerializer.class)
  @JsonDeserialize(as = XChainClaimId.class, using = XChainClaimIdDeserializer.class)
  @Beta
  abstract static class _XChainClaimId extends Wrapper<UnsignedLong> implements Serializable {

    @Override
    public String toString() {
      return this.value().toString();
    }

  }

  /**
   * A wrapped {@link com.google.common.primitives.UnsignedLong} representing a counter for XLS-38 sidechains. This
   * wrapper mostly exists to ensure we serialize fields of this type as a hex String in JSON, as these fields are
   * STUInt64s in rippled, which are hex encoded in JSON.
   *
   * <p>This class will be marked {@link com.google.common.annotations.Beta} until the featureXChainBridge amendment is
   * enabled on mainnet. Its API is subject to change.</p>
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = XChainCount.class, using = XChainCountSerializer.class)
  @JsonDeserialize(as = XChainCount.class, using = XChainCountDeserializer.class)
  @Beta
  abstract static class _XChainCount extends Wrapper<UnsignedLong> implements Serializable {

    @Override
    public String toString() {
      return this.value().toString();
    }

  }

  /**
   * A wrapped {@link String} containing a DID Document.
   *
   * <p>This class will be marked {@link com.google.common.annotations.Beta} until the featureDID amendment is
   * enabled on mainnet. Its API is subject to change.</p>
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = DidDocument.class, using = DidDocumentSerializer.class)
  @JsonDeserialize(as = DidDocument.class, using = DidDocumentDeserializer.class)
  @Beta
  abstract static class _DidDocument extends Wrapper<String> implements Serializable {

    @Override
    public String toString() {
      return this.value();
    }

  }

  /**
   * A wrapped {@link String} containing a DID URI.
   *
   * <p>This class will be marked {@link com.google.common.annotations.Beta} until the featureDID amendment is
   * enabled on mainnet. Its API is subject to change.</p>
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = DidUri.class, using = DidUriSerializer.class)
  @JsonDeserialize(as = DidUri.class, using = DidUriDeserializer.class)
  @Beta
  abstract static class _DidUri extends Wrapper<String> implements Serializable {

    @Override
    public String toString() {
      return this.value();
    }

  }

  /**
   * A wrapped {@link String} containing DID Data.
   *
   * <p>This class will be marked {@link com.google.common.annotations.Beta} until the featureDID amendment is
   * enabled on mainnet. Its API is subject to change.</p>
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = DidData.class, using = DidDataSerializer.class)
  @JsonDeserialize(as = DidData.class, using = DidDataDeserializer.class)
  @Beta
  abstract static class _DidData extends Wrapper<String> implements Serializable {

    @Override
    public String toString() {
      return this.value();
    }

  }

  /**
   * A wrapped {@link UnsignedInteger} containing an Oracle document ID.
   *
   * <p>This class will be marked {@link com.google.common.annotations.Beta} until the featurePriceOracle amendment is
   * enabled on mainnet. Its API is subject to change.</p>
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = OracleDocumentId.class, using = OracleDocumentIdSerializer.class)
  @JsonDeserialize(as = OracleDocumentId.class, using = OracleDocumentIdDeserializer.class)
  @Beta
  abstract static class _OracleDocumentId extends Wrapper<UnsignedInteger> implements Serializable {

    @Override
    public String toString() {
      return this.value().toString();
    }

  }

  /**
   * A wrapped {@link String} containing an Oracle provider.
   *
   * <p>This class will be marked {@link com.google.common.annotations.Beta} until the featurePriceOracle amendment is
   * enabled on mainnet. Its API is subject to change.</p>
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = OracleProvider.class, using = ToStringSerializer.class)
  @JsonDeserialize(as = OracleProvider.class, using = OracleProviderDeserializer.class)
  @Beta
  abstract static class _OracleProvider extends Wrapper<String> implements Serializable {

    @Override
    public String toString() {
      return this.value();
    }

  }

  /**
   * A wrapped {@link String} containing an Oracle URI.
   *
   * <p>This class will be marked {@link com.google.common.annotations.Beta} until the featurePriceOracle amendment is
   * enabled on mainnet. Its API is subject to change.</p>
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = OracleUri.class, using = ToStringSerializer.class)
  @JsonDeserialize(as = OracleUri.class, using = OracleUriDeserializer.class)
  @Beta
  abstract static class _OracleUri extends Wrapper<String> implements Serializable {

    @Override
    public String toString() {
      return this.value();
    }

  }

  /**
   * A wrapped {@link String} containing an Oracle asset price.
   *
   * <p>This class will be marked {@link com.google.common.annotations.Beta} until the featurePriceOracle amendment is
   * enabled on mainnet. Its API is subject to change.</p>
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = AssetPrice.class, using = AssetPriceSerializer.class)
  @JsonDeserialize(as = AssetPrice.class, using = AssetPriceDeserializer.class)
  @Beta
  abstract static class _AssetPrice extends Wrapper<UnsignedLong> implements Serializable {

    @Override
    public String toString() {
      return this.value().toString();
    }

  }
}
