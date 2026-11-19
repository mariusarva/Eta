package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: OfflineRecognizer.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\r\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B\u001b\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003¢\u0006\u0004\b\u0005\u0010\u0006J\t\u0010\r\u001a\u00020\u0003HÆ\u0003J\t\u0010\u000e\u001a\u00020\u0003HÆ\u0003J\u001d\u0010\u000f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0003HÆ\u0001J\u0014\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0013\u001a\u00020\u0014HÖ\u0081\u0004J\n\u0010\u0015\u001a\u00020\u0003HÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u001a\u0010\u0004\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\b\"\u0004\b\f\u0010\nÊ\u0001\f\b\u0017\u0012\b\b\u0018\u0012\u0004\b\u0003\u0010\u0000¨\u0006\u0016"}, d2 = {"Lcom/k2fsa/sherpa/onnx/OfflineFireRedAsrModelConfig;", "", "encoder", "", "decoder", "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", "getEncoder", "()Ljava/lang/String;", "setEncoder", "(Ljava/lang/String;)V", "getDecoder", "setDecoder", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "app", "Landroidx/compose/runtime/internal/StabilityInferred;", "parameters"}, k = 1, mv = {2, 4, 0}, xi = 48)
public final /* data */ class OfflineFireRedAsrModelConfig {
    public static final int $stable = 8;
    private java.lang.String decoder;
    private java.lang.String encoder;

    /* JADX WARN: Illegal instructions before constructor call */
    public OfflineFireRedAsrModelConfig() {
        java.lang.String str = null;
        this(str, str, 3, str);
    }

    public static /* synthetic */ com.k2fsa.sherpa.onnx.OfflineFireRedAsrModelConfig copy$default(com.k2fsa.sherpa.onnx.OfflineFireRedAsrModelConfig offlineFireRedAsrModelConfig, java.lang.String str, java.lang.String str2, int i, java.lang.Object obj) {
        if ((i & 1) != 0) {
            str = offlineFireRedAsrModelConfig.encoder;
        }
        if ((i & 2) != 0) {
            str2 = offlineFireRedAsrModelConfig.decoder;
        }
        return offlineFireRedAsrModelConfig.copy(str, str2);
    }

    /* JADX INFO: renamed from: component1, reason: from getter */
    public final java.lang.String getEncoder() {
        return this.encoder;
    }

    /* JADX INFO: renamed from: component2, reason: from getter */
    public final java.lang.String getDecoder() {
        return this.decoder;
    }

    public final com.k2fsa.sherpa.onnx.OfflineFireRedAsrModelConfig copy(java.lang.String encoder, java.lang.String decoder) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(encoder, "encoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(decoder, "decoder");
        return new com.k2fsa.sherpa.onnx.OfflineFireRedAsrModelConfig(encoder, decoder);
    }

    public boolean equals(java.lang.Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof com.k2fsa.sherpa.onnx.OfflineFireRedAsrModelConfig)) {
            return false;
        }
        com.k2fsa.sherpa.onnx.OfflineFireRedAsrModelConfig offlineFireRedAsrModelConfig = (com.k2fsa.sherpa.onnx.OfflineFireRedAsrModelConfig) other;
        return kotlin.jvm.internal.Intrinsics.areEqual(this.encoder, offlineFireRedAsrModelConfig.encoder) && kotlin.jvm.internal.Intrinsics.areEqual(this.decoder, offlineFireRedAsrModelConfig.decoder);
    }

    public int hashCode() {
        return (this.encoder.hashCode() * 31) + this.decoder.hashCode();
    }

    public java.lang.String toString() {
        return "OfflineFireRedAsrModelConfig(encoder=" + this.encoder + ", decoder=" + this.decoder + ")";
    }

    public OfflineFireRedAsrModelConfig(java.lang.String encoder, java.lang.String decoder) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(encoder, "encoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(decoder, "decoder");
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public /* synthetic */ OfflineFireRedAsrModelConfig(java.lang.String str, java.lang.String str2, int i, kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
        this((i & 1) != 0 ? "" : str, (i & 2) != 0 ? "" : str2);
    }

    public final java.lang.String getEncoder() {
        return this.encoder;
    }

    public final void setEncoder(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.encoder = str;
    }

    public final java.lang.String getDecoder() {
        return this.decoder;
    }

    public final void setDecoder(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.decoder = str;
    }
}
