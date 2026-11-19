package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: OfflineRecognizer.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0019\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B9\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b¢\u0006\u0004\b\t\u0010\nJ\t\u0010\u0019\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001a\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001b\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001c\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001d\u001a\u00020\bHÆ\u0003J;\u0010\u001e\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\bHÆ\u0001J\u0014\u0010\u001f\u001a\u00020\b2\b\u0010 \u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010!\u001a\u00020\"HÖ\u0081\u0004J\n\u0010#\u001a\u00020\u0003HÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u001a\u0010\u0004\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000f\u0010\f\"\u0004\b\u0010\u0010\u000eR\u001a\u0010\u0005\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\f\"\u0004\b\u0012\u0010\u000eR\u001a\u0010\u0006\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\f\"\u0004\b\u0014\u0010\u000eR\u001a\u0010\u0007\u001a\u00020\bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0015\u0010\u0016\"\u0004\b\u0017\u0010\u0018Ê\u0001\f\b%\u0012\b\b&\u0012\u0004\b\u0003\u0010\u0000¨\u0006$"}, d2 = {"Lcom/k2fsa/sherpa/onnx/OfflineCanaryModelConfig;", "", "encoder", "", "decoder", "srcLang", "tgtLang", "usePnc", "", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V", "getEncoder", "()Ljava/lang/String;", "setEncoder", "(Ljava/lang/String;)V", "getDecoder", "setDecoder", "getSrcLang", "setSrcLang", "getTgtLang", "setTgtLang", "getUsePnc", "()Z", "setUsePnc", "(Z)V", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "other", "hashCode", "", "toString", "app", "Landroidx/compose/runtime/internal/StabilityInferred;", "parameters"}, k = 1, mv = {2, 4, 0}, xi = 48)
public final /* data */ class OfflineCanaryModelConfig {
    public static final int $stable = 8;
    private java.lang.String decoder;
    private java.lang.String encoder;
    private java.lang.String srcLang;
    private java.lang.String tgtLang;
    private boolean usePnc;

    public OfflineCanaryModelConfig() {
        this(null, null, null, null, false, 31, null);
    }

    public static /* synthetic */ com.k2fsa.sherpa.onnx.OfflineCanaryModelConfig copy$default(com.k2fsa.sherpa.onnx.OfflineCanaryModelConfig offlineCanaryModelConfig, java.lang.String str, java.lang.String str2, java.lang.String str3, java.lang.String str4, boolean z, int i, java.lang.Object obj) {
        if ((i & 1) != 0) {
            str = offlineCanaryModelConfig.encoder;
        }
        if ((i & 2) != 0) {
            str2 = offlineCanaryModelConfig.decoder;
        }
        if ((i & 4) != 0) {
            str3 = offlineCanaryModelConfig.srcLang;
        }
        if ((i & 8) != 0) {
            str4 = offlineCanaryModelConfig.tgtLang;
        }
        if ((i & 16) != 0) {
            z = offlineCanaryModelConfig.usePnc;
        }
        boolean z2 = z;
        java.lang.String str5 = str3;
        return offlineCanaryModelConfig.copy(str, str2, str5, str4, z2);
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
    public final java.lang.String getSrcLang() {
        return this.srcLang;
    }

    /* JADX INFO: renamed from: component4, reason: from getter */
    public final java.lang.String getTgtLang() {
        return this.tgtLang;
    }

    /* JADX INFO: renamed from: component5, reason: from getter */
    public final boolean getUsePnc() {
        return this.usePnc;
    }

    public final com.k2fsa.sherpa.onnx.OfflineCanaryModelConfig copy(java.lang.String encoder, java.lang.String decoder, java.lang.String srcLang, java.lang.String tgtLang, boolean usePnc) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(encoder, "encoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(decoder, "decoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(srcLang, "srcLang");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(tgtLang, "tgtLang");
        return new com.k2fsa.sherpa.onnx.OfflineCanaryModelConfig(encoder, decoder, srcLang, tgtLang, usePnc);
    }

    public boolean equals(java.lang.Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof com.k2fsa.sherpa.onnx.OfflineCanaryModelConfig)) {
            return false;
        }
        com.k2fsa.sherpa.onnx.OfflineCanaryModelConfig offlineCanaryModelConfig = (com.k2fsa.sherpa.onnx.OfflineCanaryModelConfig) other;
        return kotlin.jvm.internal.Intrinsics.areEqual(this.encoder, offlineCanaryModelConfig.encoder) && kotlin.jvm.internal.Intrinsics.areEqual(this.decoder, offlineCanaryModelConfig.decoder) && kotlin.jvm.internal.Intrinsics.areEqual(this.srcLang, offlineCanaryModelConfig.srcLang) && kotlin.jvm.internal.Intrinsics.areEqual(this.tgtLang, offlineCanaryModelConfig.tgtLang) && this.usePnc == offlineCanaryModelConfig.usePnc;
    }

    public int hashCode() {
        return (((((((this.encoder.hashCode() * 31) + this.decoder.hashCode()) * 31) + this.srcLang.hashCode()) * 31) + this.tgtLang.hashCode()) * 31) + java.lang.Boolean.hashCode(this.usePnc);
    }

    public java.lang.String toString() {
        return "OfflineCanaryModelConfig(encoder=" + this.encoder + ", decoder=" + this.decoder + ", srcLang=" + this.srcLang + ", tgtLang=" + this.tgtLang + ", usePnc=" + this.usePnc + ")";
    }

    public OfflineCanaryModelConfig(java.lang.String encoder, java.lang.String decoder, java.lang.String srcLang, java.lang.String tgtLang, boolean usePnc) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(encoder, "encoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(decoder, "decoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(srcLang, "srcLang");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(tgtLang, "tgtLang");
        this.encoder = encoder;
        this.decoder = decoder;
        this.srcLang = srcLang;
        this.tgtLang = tgtLang;
        this.usePnc = usePnc;
    }

    public /* synthetic */ OfflineCanaryModelConfig(java.lang.String str, java.lang.String str2, java.lang.String str3, java.lang.String str4, boolean z, int i, kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
        this((i & 1) != 0 ? "" : str, (i & 2) != 0 ? "" : str2, (i & 4) != 0 ? "en" : str3, (i & 8) != 0 ? "en" : str4, (i & 16) != 0 ? true : z);
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

    public final java.lang.String getSrcLang() {
        return this.srcLang;
    }

    public final void setSrcLang(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.srcLang = str;
    }

    public final java.lang.String getTgtLang() {
        return this.tgtLang;
    }

    public final void setTgtLang(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.tgtLang = str;
    }

    public final boolean getUsePnc() {
        return this.usePnc;
    }

    public final void setUsePnc(boolean z) {
        this.usePnc = z;
    }
}
