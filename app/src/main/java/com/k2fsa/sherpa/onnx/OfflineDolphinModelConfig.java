package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: OfflineRecognizer.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B\u0011\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003¢\u0006\u0004\b\u0004\u0010\u0005J\t\u0010\t\u001a\u00020\u0003HÆ\u0003J\u0013\u0010\n\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003HÆ\u0001J\u0014\u0010\u000b\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u000e\u001a\u00020\u000fHÖ\u0081\u0004J\n\u0010\u0010\u001a\u00020\u0003HÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\u0005Ê\u0001\f\b\u0012\u0012\b\b\u0013\u0012\u0004\b\u0003\u0010\u0000¨\u0006\u0011"}, d2 = {"Lcom/k2fsa/sherpa/onnx/OfflineDolphinModelConfig;", "", "model", "", "<init>", "(Ljava/lang/String;)V", "getModel", "()Ljava/lang/String;", "setModel", "component1", "copy", "equals", "", "other", "hashCode", "", "toString", "app", "Landroidx/compose/runtime/internal/StabilityInferred;", "parameters"}, k = 1, mv = {2, 4, 0}, xi = 48)
public final /* data */ class OfflineDolphinModelConfig {
    public static final int $stable = 8;
    private java.lang.String model;

    /* JADX WARN: Illegal instructions before constructor call */
    public OfflineDolphinModelConfig() {
        java.lang.String str = null;
        this(str, 1, str);
    }

    public static /* synthetic */ com.k2fsa.sherpa.onnx.OfflineDolphinModelConfig copy$default(com.k2fsa.sherpa.onnx.OfflineDolphinModelConfig offlineDolphinModelConfig, java.lang.String str, int i, java.lang.Object obj) {
        if ((i & 1) != 0) {
            str = offlineDolphinModelConfig.model;
        }
        return offlineDolphinModelConfig.copy(str);
    }

    /* JADX INFO: renamed from: component1, reason: from getter */
    public final java.lang.String getModel() {
        return this.model;
    }

    public final com.k2fsa.sherpa.onnx.OfflineDolphinModelConfig copy(java.lang.String model) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(model, "model");
        return new com.k2fsa.sherpa.onnx.OfflineDolphinModelConfig(model);
    }

    public boolean equals(java.lang.Object other) {
        if (this == other) {
            return true;
        }
        return (other instanceof com.k2fsa.sherpa.onnx.OfflineDolphinModelConfig) && kotlin.jvm.internal.Intrinsics.areEqual(this.model, ((com.k2fsa.sherpa.onnx.OfflineDolphinModelConfig) other).model);
    }

    public int hashCode() {
        return this.model.hashCode();
    }

    public java.lang.String toString() {
        return "OfflineDolphinModelConfig(model=" + this.model + ")";
    }

    public OfflineDolphinModelConfig(java.lang.String model) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(model, "model");
        this.model = model;
    }

    public /* synthetic */ OfflineDolphinModelConfig(java.lang.String str, int i, kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
        this((i & 1) != 0 ? "" : str);
    }

    public final java.lang.String getModel() {
        return this.model;
    }

    public final void setModel(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.model = str;
    }
}
