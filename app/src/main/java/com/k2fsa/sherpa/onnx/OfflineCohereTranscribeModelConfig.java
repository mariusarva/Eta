package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: OfflineRecognizer.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u001a\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B9\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\u0007¢\u0006\u0004\b\t\u0010\nJ\t\u0010\u0019\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001a\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001b\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001c\u001a\u00020\u0007HÆ\u0003J\t\u0010\u001d\u001a\u00020\u0007HÆ\u0003J;\u0010\u001e\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u0007HÆ\u0001J\u0014\u0010\u001f\u001a\u00020\u00072\b\u0010 \u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010!\u001a\u00020\"HÖ\u0081\u0004J\n\u0010#\u001a\u00020\u0003HÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u001a\u0010\u0004\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000f\u0010\f\"\u0004\b\u0010\u0010\u000eR\u001a\u0010\u0005\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\f\"\u0004\b\u0012\u0010\u000eR\u001a\u0010\u0006\u001a\u00020\u0007X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\u0014\"\u0004\b\u0015\u0010\u0016R\u001a\u0010\b\u001a\u00020\u0007X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0014\"\u0004\b\u0018\u0010\u0016Ê\u0001\f\b%\u0012\b\b&\u0012\u0004\b\u0003\u0010\u0000¨\u0006$"}, d2 = {"Lcom/k2fsa/sherpa/onnx/OfflineCohereTranscribeModelConfig;", "", "encoder", "", "decoder", "language", "usePunct", "", "useItn", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZZ)V", "getEncoder", "()Ljava/lang/String;", "setEncoder", "(Ljava/lang/String;)V", "getDecoder", "setDecoder", "getLanguage", "setLanguage", "getUsePunct", "()Z", "setUsePunct", "(Z)V", "getUseItn", "setUseItn", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "other", "hashCode", "", "toString", "app", "Landroidx/compose/runtime/internal/StabilityInferred;", "parameters"}, k = 1, mv = {2, 4, 0}, xi = 48)
public final /* data */ class OfflineCohereTranscribeModelConfig {
    public static final int $stable = 8;
    private java.lang.String decoder;
    private java.lang.String encoder;
    private java.lang.String language;
    private boolean useItn;
    private boolean usePunct;

    public OfflineCohereTranscribeModelConfig() {
        this(null, null, null, false, false, 31, null);
    }

    public static /* synthetic */ com.k2fsa.sherpa.onnx.OfflineCohereTranscribeModelConfig copy$default(com.k2fsa.sherpa.onnx.OfflineCohereTranscribeModelConfig offlineCohereTranscribeModelConfig, java.lang.String str, java.lang.String str2, java.lang.String str3, boolean z, boolean z2, int i, java.lang.Object obj) {
        if ((i & 1) != 0) {
            str = offlineCohereTranscribeModelConfig.encoder;
        }
        if ((i & 2) != 0) {
            str2 = offlineCohereTranscribeModelConfig.decoder;
        }
        if ((i & 4) != 0) {
            str3 = offlineCohereTranscribeModelConfig.language;
        }
        if ((i & 8) != 0) {
            z = offlineCohereTranscribeModelConfig.usePunct;
        }
        if ((i & 16) != 0) {
            z2 = offlineCohereTranscribeModelConfig.useItn;
        }
        boolean z3 = z2;
        java.lang.String str4 = str3;
        return offlineCohereTranscribeModelConfig.copy(str, str2, str4, z, z3);
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
    public final java.lang.String getLanguage() {
        return this.language;
    }

    /* JADX INFO: renamed from: component4, reason: from getter */
    public final boolean getUsePunct() {
        return this.usePunct;
    }

    /* JADX INFO: renamed from: component5, reason: from getter */
    public final boolean getUseItn() {
        return this.useItn;
    }

    public final com.k2fsa.sherpa.onnx.OfflineCohereTranscribeModelConfig copy(java.lang.String encoder, java.lang.String decoder, java.lang.String language, boolean usePunct, boolean useItn) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(encoder, "encoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(decoder, "decoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(language, "language");
        return new com.k2fsa.sherpa.onnx.OfflineCohereTranscribeModelConfig(encoder, decoder, language, usePunct, useItn);
    }

    public boolean equals(java.lang.Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof com.k2fsa.sherpa.onnx.OfflineCohereTranscribeModelConfig)) {
            return false;
        }
        com.k2fsa.sherpa.onnx.OfflineCohereTranscribeModelConfig offlineCohereTranscribeModelConfig = (com.k2fsa.sherpa.onnx.OfflineCohereTranscribeModelConfig) other;
        return kotlin.jvm.internal.Intrinsics.areEqual(this.encoder, offlineCohereTranscribeModelConfig.encoder) && kotlin.jvm.internal.Intrinsics.areEqual(this.decoder, offlineCohereTranscribeModelConfig.decoder) && kotlin.jvm.internal.Intrinsics.areEqual(this.language, offlineCohereTranscribeModelConfig.language) && this.usePunct == offlineCohereTranscribeModelConfig.usePunct && this.useItn == offlineCohereTranscribeModelConfig.useItn;
    }

    public int hashCode() {
        return (((((((this.encoder.hashCode() * 31) + this.decoder.hashCode()) * 31) + this.language.hashCode()) * 31) + java.lang.Boolean.hashCode(this.usePunct)) * 31) + java.lang.Boolean.hashCode(this.useItn);
    }

    public java.lang.String toString() {
        return "OfflineCohereTranscribeModelConfig(encoder=" + this.encoder + ", decoder=" + this.decoder + ", language=" + this.language + ", usePunct=" + this.usePunct + ", useItn=" + this.useItn + ")";
    }

    public OfflineCohereTranscribeModelConfig(java.lang.String encoder, java.lang.String decoder, java.lang.String language, boolean usePunct, boolean useItn) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(encoder, "encoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(decoder, "decoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(language, "language");
        this.encoder = encoder;
        this.decoder = decoder;
        this.language = language;
        this.usePunct = usePunct;
        this.useItn = useItn;
    }

    public /* synthetic */ OfflineCohereTranscribeModelConfig(java.lang.String str, java.lang.String str2, java.lang.String str3, boolean z, boolean z2, int i, kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
        this((i & 1) != 0 ? "" : str, (i & 2) != 0 ? "" : str2, (i & 4) != 0 ? "" : str3, (i & 8) != 0 ? true : z, (i & 16) != 0 ? true : z2);
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

    public final java.lang.String getLanguage() {
        return this.language;
    }

    public final void setLanguage(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.language = str;
    }

    public final boolean getUsePunct() {
        return this.usePunct;
    }

    public final void setUsePunct(boolean z) {
        this.usePunct = z;
    }

    public final boolean getUseItn() {
        return this.useItn;
    }

    public final void setUseItn(boolean z) {
        this.useItn = z;
    }
}
