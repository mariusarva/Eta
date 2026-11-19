package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: OfflineRecognizer.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b+\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001Bk\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\b\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\u000b\u0012\b\b\u0002\u0010\r\u001a\u00020\b\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u0003¢\u0006\u0004\b\u000f\u0010\u0010J\t\u0010+\u001a\u00020\u0003HÆ\u0003J\t\u0010,\u001a\u00020\u0003HÆ\u0003J\t\u0010-\u001a\u00020\u0003HÆ\u0003J\t\u0010.\u001a\u00020\u0003HÆ\u0003J\t\u0010/\u001a\u00020\bHÆ\u0003J\t\u00100\u001a\u00020\bHÆ\u0003J\t\u00101\u001a\u00020\u000bHÆ\u0003J\t\u00102\u001a\u00020\u000bHÆ\u0003J\t\u00103\u001a\u00020\bHÆ\u0003J\t\u00104\u001a\u00020\u0003HÆ\u0003Jm\u00105\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\b2\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\u000b2\b\b\u0002\u0010\r\u001a\u00020\b2\b\b\u0002\u0010\u000e\u001a\u00020\u0003HÆ\u0001J\u0014\u00106\u001a\u0002072\b\u00108\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u00109\u001a\u00020\bHÖ\u0081\u0004J\n\u0010:\u001a\u00020\u0003HÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R\u001a\u0010\u0004\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0015\u0010\u0012\"\u0004\b\u0016\u0010\u0014R\u001a\u0010\u0005\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0012\"\u0004\b\u0018\u0010\u0014R\u001a\u0010\u0006\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0019\u0010\u0012\"\u0004\b\u001a\u0010\u0014R\u001a\u0010\u0007\u001a\u00020\bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001b\u0010\u001c\"\u0004\b\u001d\u0010\u001eR\u001a\u0010\t\u001a\u00020\bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010\u001c\"\u0004\b \u0010\u001eR\u001a\u0010\n\u001a\u00020\u000bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b!\u0010\"\"\u0004\b#\u0010$R\u001a\u0010\f\u001a\u00020\u000bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b%\u0010\"\"\u0004\b&\u0010$R\u001a\u0010\r\u001a\u00020\bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b'\u0010\u001c\"\u0004\b(\u0010\u001eR\u001a\u0010\u000e\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b)\u0010\u0012\"\u0004\b*\u0010\u0014Ê\u0001\f\b<\u0012\b\b=\u0012\u0004\b\u0003\u0010\u0000¨\u0006;"}, d2 = {"Lcom/k2fsa/sherpa/onnx/OfflineQwen3AsrModelConfig;", "", "convFrontend", "", "encoder", "decoder", "tokenizer", "maxTotalLen", "", "maxNewTokens", "temperature", "", "topP", "seed", "hotwords", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IIFFILjava/lang/String;)V", "getConvFrontend", "()Ljava/lang/String;", "setConvFrontend", "(Ljava/lang/String;)V", "getEncoder", "setEncoder", "getDecoder", "setDecoder", "getTokenizer", "setTokenizer", "getMaxTotalLen", "()I", "setMaxTotalLen", "(I)V", "getMaxNewTokens", "setMaxNewTokens", "getTemperature", "()F", "setTemperature", "(F)V", "getTopP", "setTopP", "getSeed", "setSeed", "getHotwords", "setHotwords", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "copy", "equals", "", "other", "hashCode", "toString", "app", "Landroidx/compose/runtime/internal/StabilityInferred;", "parameters"}, k = 1, mv = {2, 4, 0}, xi = 48)
public final /* data */ class OfflineQwen3AsrModelConfig {
    public static final int $stable = 8;
    private java.lang.String convFrontend;
    private java.lang.String decoder;
    private java.lang.String encoder;
    private java.lang.String hotwords;
    private int maxNewTokens;
    private int maxTotalLen;
    private int seed;
    private float temperature;
    private java.lang.String tokenizer;
    private float topP;

    public OfflineQwen3AsrModelConfig() {
        this(null, null, null, null, 0, 0, 0.0f, 0.0f, 0, null, 1023, null);
    }

    public static /* synthetic */ com.k2fsa.sherpa.onnx.OfflineQwen3AsrModelConfig copy$default(com.k2fsa.sherpa.onnx.OfflineQwen3AsrModelConfig offlineQwen3AsrModelConfig, java.lang.String str, java.lang.String str2, java.lang.String str3, java.lang.String str4, int i, int i2, float f, float f2, int i3, java.lang.String str5, int i4, java.lang.Object obj) {
        if ((i4 & 1) != 0) {
            str = offlineQwen3AsrModelConfig.convFrontend;
        }
        if ((i4 & 2) != 0) {
            str2 = offlineQwen3AsrModelConfig.encoder;
        }
        if ((i4 & 4) != 0) {
            str3 = offlineQwen3AsrModelConfig.decoder;
        }
        if ((i4 & 8) != 0) {
            str4 = offlineQwen3AsrModelConfig.tokenizer;
        }
        if ((i4 & 16) != 0) {
            i = offlineQwen3AsrModelConfig.maxTotalLen;
        }
        if ((i4 & 32) != 0) {
            i2 = offlineQwen3AsrModelConfig.maxNewTokens;
        }
        if ((i4 & 64) != 0) {
            f = offlineQwen3AsrModelConfig.temperature;
        }
        if ((i4 & 128) != 0) {
            f2 = offlineQwen3AsrModelConfig.topP;
        }
        if ((i4 & io.github.mangi.eta.agent.voice.local.LocalSpeechVoskArchive.MAX_ENTRIES) != 0) {
            i3 = offlineQwen3AsrModelConfig.seed;
        }
        if ((i4 & io.github.mangi.eta.agent.web.WebPageContent.MAX_TITLE_CHARS) != 0) {
            str5 = offlineQwen3AsrModelConfig.hotwords;
        }
        int i5 = i3;
        java.lang.String str6 = str5;
        float f3 = f;
        float f4 = f2;
        int i6 = i;
        int i7 = i2;
        return offlineQwen3AsrModelConfig.copy(str, str2, str3, str4, i6, i7, f3, f4, i5, str6);
    }

    /* JADX INFO: renamed from: component1, reason: from getter */
    public final java.lang.String getConvFrontend() {
        return this.convFrontend;
    }

    /* JADX INFO: renamed from: component10, reason: from getter */
    public final java.lang.String getHotwords() {
        return this.hotwords;
    }

    /* JADX INFO: renamed from: component2, reason: from getter */
    public final java.lang.String getEncoder() {
        return this.encoder;
    }

    /* JADX INFO: renamed from: component3, reason: from getter */
    public final java.lang.String getDecoder() {
        return this.decoder;
    }

    /* JADX INFO: renamed from: component4, reason: from getter */
    public final java.lang.String getTokenizer() {
        return this.tokenizer;
    }

    /* JADX INFO: renamed from: component5, reason: from getter */
    public final int getMaxTotalLen() {
        return this.maxTotalLen;
    }

    /* JADX INFO: renamed from: component6, reason: from getter */
    public final int getMaxNewTokens() {
        return this.maxNewTokens;
    }

    /* JADX INFO: renamed from: component7, reason: from getter */
    public final float getTemperature() {
        return this.temperature;
    }

    /* JADX INFO: renamed from: component8, reason: from getter */
    public final float getTopP() {
        return this.topP;
    }

    /* JADX INFO: renamed from: component9, reason: from getter */
    public final int getSeed() {
        return this.seed;
    }

    public final com.k2fsa.sherpa.onnx.OfflineQwen3AsrModelConfig copy(java.lang.String convFrontend, java.lang.String encoder, java.lang.String decoder, java.lang.String tokenizer, int maxTotalLen, int maxNewTokens, float temperature, float topP, int seed, java.lang.String hotwords) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(convFrontend, "convFrontend");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(encoder, "encoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(decoder, "decoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(tokenizer, "tokenizer");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(hotwords, "hotwords");
        return new com.k2fsa.sherpa.onnx.OfflineQwen3AsrModelConfig(convFrontend, encoder, decoder, tokenizer, maxTotalLen, maxNewTokens, temperature, topP, seed, hotwords);
    }

    public boolean equals(java.lang.Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof com.k2fsa.sherpa.onnx.OfflineQwen3AsrModelConfig)) {
            return false;
        }
        com.k2fsa.sherpa.onnx.OfflineQwen3AsrModelConfig offlineQwen3AsrModelConfig = (com.k2fsa.sherpa.onnx.OfflineQwen3AsrModelConfig) other;
        return kotlin.jvm.internal.Intrinsics.areEqual(this.convFrontend, offlineQwen3AsrModelConfig.convFrontend) && kotlin.jvm.internal.Intrinsics.areEqual(this.encoder, offlineQwen3AsrModelConfig.encoder) && kotlin.jvm.internal.Intrinsics.areEqual(this.decoder, offlineQwen3AsrModelConfig.decoder) && kotlin.jvm.internal.Intrinsics.areEqual(this.tokenizer, offlineQwen3AsrModelConfig.tokenizer) && this.maxTotalLen == offlineQwen3AsrModelConfig.maxTotalLen && this.maxNewTokens == offlineQwen3AsrModelConfig.maxNewTokens && java.lang.Float.compare(this.temperature, offlineQwen3AsrModelConfig.temperature) == 0 && java.lang.Float.compare(this.topP, offlineQwen3AsrModelConfig.topP) == 0 && this.seed == offlineQwen3AsrModelConfig.seed && kotlin.jvm.internal.Intrinsics.areEqual(this.hotwords, offlineQwen3AsrModelConfig.hotwords);
    }

    public int hashCode() {
        return (((((((((((((((((this.convFrontend.hashCode() * 31) + this.encoder.hashCode()) * 31) + this.decoder.hashCode()) * 31) + this.tokenizer.hashCode()) * 31) + java.lang.Integer.hashCode(this.maxTotalLen)) * 31) + java.lang.Integer.hashCode(this.maxNewTokens)) * 31) + java.lang.Float.hashCode(this.temperature)) * 31) + java.lang.Float.hashCode(this.topP)) * 31) + java.lang.Integer.hashCode(this.seed)) * 31) + this.hotwords.hashCode();
    }

    public java.lang.String toString() {
        return "OfflineQwen3AsrModelConfig(convFrontend=" + this.convFrontend + ", encoder=" + this.encoder + ", decoder=" + this.decoder + ", tokenizer=" + this.tokenizer + ", maxTotalLen=" + this.maxTotalLen + ", maxNewTokens=" + this.maxNewTokens + ", temperature=" + this.temperature + ", topP=" + this.topP + ", seed=" + this.seed + ", hotwords=" + this.hotwords + ")";
    }

    public OfflineQwen3AsrModelConfig(java.lang.String convFrontend, java.lang.String encoder, java.lang.String decoder, java.lang.String tokenizer, int maxTotalLen, int maxNewTokens, float temperature, float topP, int seed, java.lang.String hotwords) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(convFrontend, "convFrontend");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(encoder, "encoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(decoder, "decoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(tokenizer, "tokenizer");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(hotwords, "hotwords");
        this.convFrontend = convFrontend;
        this.encoder = encoder;
        this.decoder = decoder;
        this.tokenizer = tokenizer;
        this.maxTotalLen = maxTotalLen;
        this.maxNewTokens = maxNewTokens;
        this.temperature = temperature;
        this.topP = topP;
        this.seed = seed;
        this.hotwords = hotwords;
    }

    public /* synthetic */ OfflineQwen3AsrModelConfig(java.lang.String str, java.lang.String str2, java.lang.String str3, java.lang.String str4, int i, int i2, float f, float f2, int i3, java.lang.String str5, int i4, kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
        this((i4 & 1) != 0 ? "" : str, (i4 & 2) != 0 ? "" : str2, (i4 & 4) != 0 ? "" : str3, (i4 & 8) != 0 ? "" : str4, (i4 & 16) != 0 ? 512 : i, (i4 & 32) != 0 ? 128 : i2, (i4 & 64) != 0 ? 1.0E-6f : f, (i4 & 128) != 0 ? 0.8f : f2, (i4 & io.github.mangi.eta.agent.voice.local.LocalSpeechVoskArchive.MAX_ENTRIES) != 0 ? 42 : i3, (i4 & io.github.mangi.eta.agent.web.WebPageContent.MAX_TITLE_CHARS) != 0 ? "" : str5);
    }

    public final java.lang.String getConvFrontend() {
        return this.convFrontend;
    }

    public final void setConvFrontend(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.convFrontend = str;
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

    public final java.lang.String getTokenizer() {
        return this.tokenizer;
    }

    public final void setTokenizer(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.tokenizer = str;
    }

    public final int getMaxTotalLen() {
        return this.maxTotalLen;
    }

    public final void setMaxTotalLen(int i) {
        this.maxTotalLen = i;
    }

    public final int getMaxNewTokens() {
        return this.maxNewTokens;
    }

    public final void setMaxNewTokens(int i) {
        this.maxNewTokens = i;
    }

    public final float getTemperature() {
        return this.temperature;
    }

    public final void setTemperature(float f) {
        this.temperature = f;
    }

    public final float getTopP() {
        return this.topP;
    }

    public final void setTopP(float f) {
        this.topP = f;
    }

    public final int getSeed() {
        return this.seed;
    }

    public final void setSeed(int i) {
        this.seed = i;
    }

    public final java.lang.String getHotwords() {
        return this.hotwords;
    }

    public final void setHotwords(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.hotwords = str;
    }
}
