package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: OfflineRecognizer.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B\u001b\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\t\u0010\u0010\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0011\u001a\u00020\u0005HÆ\u0003J\u001d\u0010\u0012\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005HÆ\u0001J\u0014\u0010\u0013\u001a\u00020\u00142\b\u0010\u0015\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0016\u001a\u00020\u0017HÖ\u0081\u0004J\n\u0010\u0018\u001a\u00020\u0003HÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000fÊ\u0001\f\b\u001a\u0012\b\b\u001b\u0012\u0004\b\u0003\u0010\u0000¨\u0006\u0019"}, d2 = {"Lcom/k2fsa/sherpa/onnx/OfflineParaformerModelConfig;", "", "model", "", "qnnConfig", "Lcom/k2fsa/sherpa/onnx/QnnConfig;", "<init>", "(Ljava/lang/String;Lcom/k2fsa/sherpa/onnx/QnnConfig;)V", "getModel", "()Ljava/lang/String;", "setModel", "(Ljava/lang/String;)V", "getQnnConfig", "()Lcom/k2fsa/sherpa/onnx/QnnConfig;", "setQnnConfig", "(Lcom/k2fsa/sherpa/onnx/QnnConfig;)V", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "app", "Landroidx/compose/runtime/internal/StabilityInferred;", "parameters"}, k = 1, mv = {2, 4, 0}, xi = 48)
public final /* data */ class OfflineParaformerModelConfig {
    public static final int $stable = 8;
    private java.lang.String model;
    private com.k2fsa.sherpa.onnx.QnnConfig qnnConfig;

    /* JADX WARN: Multi-variable type inference failed */
    public OfflineParaformerModelConfig() {
        this(null, 0 == true ? 1 : 0, 3, 0 == true ? 1 : 0);
    }

    public static /* synthetic */ com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig copy$default(com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig offlineParaformerModelConfig, java.lang.String str, com.k2fsa.sherpa.onnx.QnnConfig qnnConfig, int i, java.lang.Object obj) {
        if ((i & 1) != 0) {
            str = offlineParaformerModelConfig.model;
        }
        if ((i & 2) != 0) {
            qnnConfig = offlineParaformerModelConfig.qnnConfig;
        }
        return offlineParaformerModelConfig.copy(str, qnnConfig);
    }

    /* JADX INFO: renamed from: component1, reason: from getter */
    public final java.lang.String getModel() {
        return this.model;
    }

    /* JADX INFO: renamed from: component2, reason: from getter */
    public final com.k2fsa.sherpa.onnx.QnnConfig getQnnConfig() {
        return this.qnnConfig;
    }

    public final com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig copy(java.lang.String model, com.k2fsa.sherpa.onnx.QnnConfig qnnConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(model, "model");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(qnnConfig, "qnnConfig");
        return new com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig(model, qnnConfig);
    }

    public boolean equals(java.lang.Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig)) {
            return false;
        }
        com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig offlineParaformerModelConfig = (com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig) other;
        return kotlin.jvm.internal.Intrinsics.areEqual(this.model, offlineParaformerModelConfig.model) && kotlin.jvm.internal.Intrinsics.areEqual(this.qnnConfig, offlineParaformerModelConfig.qnnConfig);
    }

    public int hashCode() {
        return (this.model.hashCode() * 31) + this.qnnConfig.hashCode();
    }

    public java.lang.String toString() {
        return "OfflineParaformerModelConfig(model=" + this.model + ", qnnConfig=" + this.qnnConfig + ")";
    }

    public OfflineParaformerModelConfig(java.lang.String model, com.k2fsa.sherpa.onnx.QnnConfig qnnConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(model, "model");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(qnnConfig, "qnnConfig");
        this.model = model;
        this.qnnConfig = qnnConfig;
    }

    public /* synthetic */ OfflineParaformerModelConfig(java.lang.String str, com.k2fsa.sherpa.onnx.QnnConfig qnnConfig, int i, kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
        this((i & 1) != 0 ? "" : str, (i & 2) != 0 ? new com.k2fsa.sherpa.onnx.QnnConfig(null, null, null, 7, null) : qnnConfig);
    }

    public final java.lang.String getModel() {
        return this.model;
    }

    public final void setModel(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.model = str;
    }

    public final com.k2fsa.sherpa.onnx.QnnConfig getQnnConfig() {
        return this.qnnConfig;
    }

    public final void setQnnConfig(com.k2fsa.sherpa.onnx.QnnConfig qnnConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(qnnConfig, "<set-?>");
        this.qnnConfig = qnnConfig;
    }
}
