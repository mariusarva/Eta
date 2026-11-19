package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: OfflineRecognizer.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0014\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B/\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007¢\u0006\u0004\b\b\u0010\tJ\t\u0010\u0016\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0017\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0018\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0019\u001a\u00020\u0007HÆ\u0003J1\u0010\u001a\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u0007HÆ\u0001J\u0014\u0010\u001b\u001a\u00020\u001c2\b\u0010\u001d\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u001e\u001a\u00020\u001fHÖ\u0081\u0004J\n\u0010 \u001a\u00020\u0003HÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\rR\u001a\u0010\u0004\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u000b\"\u0004\b\u000f\u0010\rR\u001a\u0010\u0005\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u000b\"\u0004\b\u0011\u0010\rR\u001a\u0010\u0006\u001a\u00020\u0007X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u0013\"\u0004\b\u0014\u0010\u0015Ê\u0001\f\b\"\u0012\b\b#\u0012\u0004\b\u0003\u0010\u0000¨\u0006!"}, d2 = {"Lcom/k2fsa/sherpa/onnx/OfflineTransducerModelConfig;", "", "encoder", "", "decoder", "joiner", "qnnConfig", "Lcom/k2fsa/sherpa/onnx/QnnConfig;", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/k2fsa/sherpa/onnx/QnnConfig;)V", "getEncoder", "()Ljava/lang/String;", "setEncoder", "(Ljava/lang/String;)V", "getDecoder", "setDecoder", "getJoiner", "setJoiner", "getQnnConfig", "()Lcom/k2fsa/sherpa/onnx/QnnConfig;", "setQnnConfig", "(Lcom/k2fsa/sherpa/onnx/QnnConfig;)V", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "", "toString", "app", "Landroidx/compose/runtime/internal/StabilityInferred;", "parameters"}, k = 1, mv = {2, 4, 0}, xi = 48)
public final /* data */ class OfflineTransducerModelConfig {
    public static final int $stable = 8;
    private java.lang.String decoder;
    private java.lang.String encoder;
    private java.lang.String joiner;
    private com.k2fsa.sherpa.onnx.QnnConfig qnnConfig;

    public OfflineTransducerModelConfig() {
        this(null, null, null, null, 15, null);
    }

    public static /* synthetic */ com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig copy$default(com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig offlineTransducerModelConfig, java.lang.String str, java.lang.String str2, java.lang.String str3, com.k2fsa.sherpa.onnx.QnnConfig qnnConfig, int i, java.lang.Object obj) {
        if ((i & 1) != 0) {
            str = offlineTransducerModelConfig.encoder;
        }
        if ((i & 2) != 0) {
            str2 = offlineTransducerModelConfig.decoder;
        }
        if ((i & 4) != 0) {
            str3 = offlineTransducerModelConfig.joiner;
        }
        if ((i & 8) != 0) {
            qnnConfig = offlineTransducerModelConfig.qnnConfig;
        }
        return offlineTransducerModelConfig.copy(str, str2, str3, qnnConfig);
    }

    /* JADX INFO: renamed from: component1, reason: from getter */
    public final java.lang.String getEncoder() {
        return this.encoder;
    }

    /* JADX INFO: renamed from: component2, reason: from getter */
    public final java.lang.String getDecoder() {
        return this.decoder;
    }

    /* JADX INFO: renamed from: component3, reason: from getter */
    public final java.lang.String getJoiner() {
        return this.joiner;
    }

    /* JADX INFO: renamed from: component4, reason: from getter */
    public final com.k2fsa.sherpa.onnx.QnnConfig getQnnConfig() {
        return this.qnnConfig;
    }

    public final com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig copy(java.lang.String encoder, java.lang.String decoder, java.lang.String joiner, com.k2fsa.sherpa.onnx.QnnConfig qnnConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(encoder, "encoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(decoder, "decoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(joiner, "joiner");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(qnnConfig, "qnnConfig");
        return new com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig(encoder, decoder, joiner, qnnConfig);
    }

    public boolean equals(java.lang.Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig)) {
            return false;
        }
        com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig offlineTransducerModelConfig = (com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig) other;
        return kotlin.jvm.internal.Intrinsics.areEqual(this.encoder, offlineTransducerModelConfig.encoder) && kotlin.jvm.internal.Intrinsics.areEqual(this.decoder, offlineTransducerModelConfig.decoder) && kotlin.jvm.internal.Intrinsics.areEqual(this.joiner, offlineTransducerModelConfig.joiner) && kotlin.jvm.internal.Intrinsics.areEqual(this.qnnConfig, offlineTransducerModelConfig.qnnConfig);
    }

    public int hashCode() {
        return (((((this.encoder.hashCode() * 31) + this.decoder.hashCode()) * 31) + this.joiner.hashCode()) * 31) + this.qnnConfig.hashCode();
    }

    public java.lang.String toString() {
        return "OfflineTransducerModelConfig(encoder=" + this.encoder + ", decoder=" + this.decoder + ", joiner=" + this.joiner + ", qnnConfig=" + this.qnnConfig + ")";
    }

    public OfflineTransducerModelConfig(java.lang.String encoder, java.lang.String decoder, java.lang.String joiner, com.k2fsa.sherpa.onnx.QnnConfig qnnConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(encoder, "encoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(decoder, "decoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(joiner, "joiner");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(qnnConfig, "qnnConfig");
        this.encoder = encoder;
        this.decoder = decoder;
        this.joiner = joiner;
        this.qnnConfig = qnnConfig;
    }

    public /* synthetic */ OfflineTransducerModelConfig(java.lang.String str, java.lang.String str2, java.lang.String str3, com.k2fsa.sherpa.onnx.QnnConfig qnnConfig, int i, kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
        this((i & 1) != 0 ? "" : str, (i & 2) != 0 ? "" : str2, (i & 4) != 0 ? "" : str3, (i & 8) != 0 ? new com.k2fsa.sherpa.onnx.QnnConfig(null, null, null, 7, null) : qnnConfig);
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

    public final java.lang.String getJoiner() {
        return this.joiner;
    }

    public final void setJoiner(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.joiner = str;
    }

    public final com.k2fsa.sherpa.onnx.QnnConfig getQnnConfig() {
        return this.qnnConfig;
    }

    public final void setQnnConfig(com.k2fsa.sherpa.onnx.QnnConfig qnnConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(qnnConfig, "<set-?>");
        this.qnnConfig = qnnConfig;
    }
}
