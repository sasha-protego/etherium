package example

import org.web3j.crypto.Sign.SignatureData
import org.web3j.crypto.{
  ECDSASignature,
  Hash,
  RawTransaction,
  Sign,
  TransactionEncoder
}
import org.web3j.rlp.{RlpEncoder, RlpList, RlpType}
import org.web3j.utils

import java.nio.ByteBuffer
import java.util

object FutureSignOps {
  def createSignatureData(
      sig: ECDSASignature,
      publicKey: BigInt,
      messageHash: Array[Byte]
  ): Sign.SignatureData = {
    // Now we have to work backwards to figure out the recId needed to recover the signature.
    val recId = (0 to 4).find(i => {
      val k = Sign.recoverFromSignature(i, sig, messageHash)
      k != null && k.equals(publicKey.bigInteger)
    }) match {
      case Some(v) => v
      case None =>
        throw new RuntimeException(
          "Could not construct a recoverable key. Are your credentials valid?"
        );
    }

    val headerByte = (recId + 27).toByte

    // 1 header + 32 bytes for R + 32 bytes for S
    val v: Array[Byte] = new Array(headerByte)
    val r = utils.Numeric.toBytesPadded(sig.r, 32)
    val s = utils.Numeric.toBytesPadded(sig.s, 32)

    new Sign.SignatureData(v, r, s);
  }
}

object FutureTransactionEncoderOps {
  def encode(
      rawTransaction: RawTransaction,
      signatureData: SignatureData
  ): Array[Byte] = {
    val values: util.List[RlpType] =
      TransactionEncoder.asRlpValues(rawTransaction, signatureData);
    val rlpList: RlpList = new RlpList(values)
    val encoded: Array[Byte] = RlpEncoder.encode(rlpList)
    val eip1559: Byte = 0x02
    ByteBuffer
      .allocate(encoded.length + 1)
      .put(eip1559)
      .put(encoded)
      .array();
  }
}

object Address {
  def fromPubKey(pubKey: String): String = {
    val hashedPubKey: String = Hash.sha3(pubKey)
    val l = hashedPubKey.length
    println("hash length: " + l)
    "0x" + hashedPubKey.substring(l - 40, l)
  }
}
object Hello extends App {
  val pubKey =
    "04D5B3DDDF6458E6B8747008673FE715A044E8D1F631DCB574C0EDE65370E1D6CE6325CE21DC46FB0E84FFECF5469CA57F8526AB4107CACD3831A4BE60BCDF8D5C"
  // val pubKey2 = "047668ACB9A29363F674567FD9DB0B640DFD1AE2C43791D8F6B82B69900485AF69397F51E9DAAB4F9E256131F1AC5026B344BED1DA9C2FB3A9E851767E7FB5F3DB"

  val address =
    Address.fromPubKey(pubKey.substring(2)) // 0x82004e7d919286fe5a9c
  println("address: " + address)
  val nonce: BigInt = 0
  val gasLimit: BigInt = 100 * 1000
  val to: String = address
  val value: BigInt = 123
  val data: String = "Protego"
  val maxPriorityFeePerGas: BigInt = 5678
  val maxFeePerGas: BigInt = 0x2f925e88
  val dev = 0x539
  val goerli = 0x5
  val txn = RawTransaction.createTransaction(
    dev,
    nonce.bigInteger,
    gasLimit.bigInteger,
    to,
    value.bigInteger,
    data,
    maxPriorityFeePerGas.bigInteger,
    maxFeePerGas.bigInteger
  )
  val encoded: Array[Byte] = TransactionEncoder.encode(txn)
  val hashed: Array[Byte] = Hash.sha3(encoded)
  printf("To be signed: %X\n", BigInt(1, hashed))
  hashed.foreach(printf("%02X", _))
  println()
  // val address2 = Address.fromPubKey(pubKey2.substring(2)) // 0x6eb268e627b786fab466
  // println("address2: " + address2)
  // val publicKey2: BigInt = BigInt(pubKey2.substring(2), 16)
  val signature =
    "BCA1830EED1A084D6AB9E567E24A25A47CA830892DFB1DC52532BF34D069B154610786212BFC9CFAF4E6A1FEB8E6E743D5068493B7D9EB55F7DB2FB3DB8DFBB3"

  println(signature)
  val r = BigInt(signature.substring(0, 64), 16)
  val s = BigInt(signature.substring(64), 16)
  printf(s"%X%X\n", r, s)
  val ecdsaSignature: ECDSASignature = new ECDSASignature(
    r.bigInteger,
    s.bigInteger
  )
  val publicKey: BigInt = BigInt(pubKey.substring(2), 16)
  val signatureData =
    Sign.createSignatureData(ecdsaSignature, publicKey.bigInteger, hashed)
  val signedTxn: Array[Byte] = TransactionEncoder.encode(txn, signatureData)
  val v = BigInt(signedTxn)
  printf("signed Txn: %X\n", v)
}
