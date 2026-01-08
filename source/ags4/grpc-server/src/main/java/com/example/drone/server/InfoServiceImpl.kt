package com.example.drone.server

import com.example.drone.*
import io.grpc.stub.StreamObserver

/**
 * InfoService (Unary RPC)
 * - getProduct(): returns vendor + product/model
 * - getIdentification(): returns serial (hardware_uid)
 *
 * SHGM spec:
 * - If no connected UAV system -> RESULT_NO_SYSTEM (or INFORMATION_NOT_RECEIVED_YET based on your design)
 */
class InfoServiceImpl(
    private val monitor: ConnectionStateMonitor
) : InfoServiceGrpc.InfoServiceImplBase() {

    override fun getProduct(
        request: GetProductRequest,
        responseObserver: StreamObserver<GetProductResponse>
    ) {
        if (!monitor.isConnected()) {
            Log.w("INFO", "getProduct denied | no system")

            val resp = GetProductResponse.newBuilder()
                .setResult(
                    InfoResult.newBuilder()
                        .setResult(InfoResult.Result.RESULT_NO_SYSTEM)
                        .setResultStr("No connected system.")
                        .build()
                )
                .setProduct(
                    Product.newBuilder()
                        .setVendorId(0)
                        .setVendorName("Unknown")
                        .setProductId(0)
                        .setProductName("Unknown")
                        .build()
                )
                .build()

            responseObserver.onNext(resp)
            responseObserver.onCompleted()
            return
        }

        // DEMO values (replace with real data source later)
        val product = Product.newBuilder()
            .setVendorId(1)
            .setVendorName("Baibars")
            .setProductId(110)
            .setProductName("CT-110")
            .build()

        val resp = GetProductResponse.newBuilder()
            .setResult(
                InfoResult.newBuilder()
                    .setResult(InfoResult.Result.RESULT_SUCCESS)
                    .setResultStr("OK")
                    .build()
            )
            .setProduct(product)
            .build()

        Log.i("INFO", "getProduct OK | vendor=${product.vendorName} model=${product.productName}")

        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }

    override fun getIdentification(
        request: GetProductRequest,
        responseObserver: StreamObserver<IdentificationResponse>
    ) {
        if (!monitor.isConnected()) {
            Log.w("INFO", "getIdentification denied | no system")

            val resp = IdentificationResponse.newBuilder()
                .setResult(
                    InfoResult.newBuilder()
                        .setResult(InfoResult.Result.RESULT_NO_SYSTEM)
                        .setResultStr("No connected system.")
                        .build()
                )
                .setHardwareUid("Unknown")
                .setLegacyUid(0)
                .build()

            responseObserver.onNext(resp)
            responseObserver.onCompleted()
            return
        }

        val resp = IdentificationResponse.newBuilder()
            .setResult(
                InfoResult.newBuilder()
                    .setResult(InfoResult.Result.RESULT_SUCCESS)
                    .setResultStr("OK")
                    .build()
            )
            .setHardwareUid("CT110-SN-000001")
            .setLegacyUid(0)
            .build()

        Log.i("INFO", "getIdentification OK | uid=${resp.hardwareUid}")

        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }
}
