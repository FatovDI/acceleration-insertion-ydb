package com.example.ydbinsertion.batchinsertion.benchmark

import com.example.ydbinsertion.batchinsertion.benchmark.blackhole.ConnectionBlackhole
import com.example.ydbinsertion.batchinsertion.benchmark.blackhole.DataOutputStreamBlackhole
import com.example.ydbinsertion.batchinsertion.getTableName
import com.example.ydbinsertion.batchinsertion.impl.processor.YDBBatchInsertionByEntityProcessor
import com.example.ydbinsertion.batchinsertion.impl.processor.YDBBatchInsertionByPropertyProcessor
import com.example.ydbinsertion.logic.entity.*
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.reflect.KMutableProperty1

@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 10)
class ProcessorBenchmark {

    @Benchmark
    fun saveDataByKotlinProperty_4_000_000(bh: Blackhole) {
        saveDataByKotlinProperty(4000000, bh)
    }

    @Benchmark
    fun saveDataWithReflection_4_000_000(bh: Blackhole) {
        saveDataByReflection(4000000, bh)
    }

    fun saveDataByReflection(count: Int, bh: Blackhole) {
        val processor = YDBBatchInsertionByEntityProcessor()

        for (i in 1..count) {
            val data = PaymentDocumentEntity(
                account = AccountEntity().apply { id = 1 },
                expense = false,
                amount = BigDecimal("10.11"),
                cur = CurrencyEntity(code = "RUB"),
                orderDate = LocalDate.parse("2023-01-01"),
                orderNumber = "123",
                prop20 = "1345",
                prop15 = "END",
                paymentPurpose = "paymentPurpose",
                prop10 = "prop10",
            )

            bh.consume(processor.getStringForInsert(data))
        }

        processor.insertDataToDataBaseMultiRow(PaymentDocumentEntity::class, listOf(), ConnectionBlackhole(bh))
    }


    fun saveDataByKotlinProperty(count: Int, bh: Blackhole) {
        val nullValue = "NULL"
        val processor = YDBBatchInsertionByPropertyProcessor()

        val data = mutableMapOf<KMutableProperty1<out BaseEntity, *>, String?>()

        for (i in 1..count) {
            data[PaymentDocumentEntity::account] = "1"
            data[PaymentDocumentEntity::amount] = "10.11"
            data[PaymentDocumentEntity::expense] = "true"
            data[PaymentDocumentEntity::cur] = "RUB"
            data[PaymentDocumentEntity::orderDate] = "2023-01-01"
            data[PaymentDocumentEntity::orderNumber] = "123"
            data[PaymentDocumentEntity::prop20] = "1345"
            data[PaymentDocumentEntity::prop15] = "END"
            data[PaymentDocumentEntity::paymentPurpose] = "paymentPurpose"
            data[PaymentDocumentEntity::prop10] = "prop10"

            bh.consume(processor.getStringForInsert(data, nullValue))
        }

        processor.insertDataToDataBaseMultiRow(PaymentDocumentEntity::class, data.keys, listOf(), ConnectionBlackhole(bh))

    }

}
