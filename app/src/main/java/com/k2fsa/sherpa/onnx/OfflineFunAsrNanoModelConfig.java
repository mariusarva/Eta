package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: OfflineRecognizer.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b8\n\u0002\u0018\u0002\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B\u0089\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0003\u0012\b\b\u0002\u0010\b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u0012\b\b\u0002\u0010\r\u001a\u00020\f\u0012\b\b\u0002\u0010\u000e\u001a\u00020\n\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0011\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u0003¢\u0006\u0004\b\u0013\u0010\u0014J\t\u00107\u001a\u00020\u0003HÆ\u0003J\t\u00108\u001a\u00020\u0003HÆ\u0003J\t\u00109\u001a\u00020\u0003HÆ\u0003J\t\u0010:\u001a\u00020\u0003HÆ\u0003J\t\u0010;\u001a\u00020\u0003HÆ\u0003J\t\u0010<\u001a\u00020\u0003HÆ\u0003J\t\u0010=\u001a\u00020\nHÆ\u0003J\t\u0010>\u001a\u00020\fHÆ\u0003J\t\u0010?\u001a\u00020\fHÆ\u0003J\t\u0010@\u001a\u00020\nHÆ\u0003J\t\u0010A\u001a\u00020\u0003HÆ\u0003J\t\u0010B\u001a\u00020\u0011HÆ\u0003J\t\u0010C\u001a\u00020\u0003HÆ\u0003J\u008b\u0001\u0010D\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\f2\b\b\u0002\u0010\u000e\u001a\u00020\n2\b\b\u0002\u0010\u000f\u001a\u00020\u00032\b\b\u0002\u0010\u0010\u001a\u00020\u00112\b\b\u0002\u0010\u0012\u001a\u00020\u0003HÆ\u0001J\u0014\u0010E\u001a\u00020\u00112\b\u0010F\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010G\u001a\u00020\nHÖ\u0081\u0004J\n\u0010H\u001a\u00020\u0003HÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0015\u0010\u0016\"\u0004\b\u0017\u0010\u0018R\u001a\u0010\u0004\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0019\u0010\u0016\"\u0004\b\u001a\u0010\u0018R\u001a\u0010\u0005\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001b\u0010\u0016\"\u0004\b\u001c\u0010\u0018R\u001a\u0010\u0006\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001d\u0010\u0016\"\u0004\b\u001e\u0010\u0018R\u001a\u0010\u0007\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010\u0016\"\u0004\b \u0010\u0018R\u001a\u0010\b\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b!\u0010\u0016\"\u0004\b\"\u0010\u0018R\u001a\u0010\t\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b#\u0010$\"\u0004\b%\u0010&R\u001a\u0010\u000b\u001a\u00020\fX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b'\u0010(\"\u0004\b)\u0010*R\u001a\u0010\r\u001a\u00020\fX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b+\u0010(\"\u0004\b,\u0010*R\u001a\u0010\u000e\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b-\u0010$\"\u0004\b.\u0010&R\u001a\u0010\u000f\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b/\u0010\u0016\"\u0004\b0\u0010\u0018R\u001a\u0010\u0010\u001a\u00020\u0011X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b1\u00102\"\u0004\b3\u00104R\u001a\u0010\u0012\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b5\u0010\u0016\"\u0004\b6\u0010\u0018Ê\u0001\f\bJ\u0012\b\bK\u0012\u0004\b\u0003\u0010\u0000¨\u0006I"}, d2 = {"Lcom/k2fsa/sherpa/onnx/OfflineFunAsrNanoModelConfig;", "", "encoderAdaptor", "", "llm", "embedding", "tokenizer", "systemPrompt", "userPrompt", "maxNewTokens", "", "temperature", "", "topP", "seed", "language", "itn", "", "hotwords", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IFFILjava/lang/String;ZLjava/lang/String;)V", "getEncoderAdaptor", "()Ljava/lang/String;", "setEncoderAdaptor", "(Ljava/lang/String;)V", "getLlm", "setLlm", "getEmbedding", "setEmbedding", "getTokenizer", "setTokenizer", "getSystemPrompt", "setSystemPrompt", "getUserPrompt", "setUserPrompt", "getMaxNewTokens", "()I", "setMaxNewTokens", "(I)V", "getTemperature", "()F", "setTemperature", "(F)V", "getTopP", "setTopP", "getSeed", "setSeed", "getLanguage", "setLanguage", "getItn", "()Z", "setItn", "(Z)V", "getHotwords", "setHotwords", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "component11", "component12", "component13", "copy", "equals", "other", "hashCode", "toString", "app", "Landroidx/compose/runtime/internal/StabilityInferred;", "parameters"}, k = 1, mv = {2, 4, 0}, xi = 48)
public final /* data */ class OfflineFunAsrNanoModelConfig {
    public static final int $stable = 8;
    private java.lang.String embedding;
    private java.lang.String encoderAdaptor;
    private java.lang.String hotwords;
    private boolean itn;
    private java.lang.String language;
    private java.lang.String llm;
    private int maxNewTokens;
    private int seed;
    private java.lang.String systemPrompt;
    private float temperature;
    private java.lang.String tokenizer;
    private float topP;
    private java.lang.String userPrompt;

    public OfflineFunAsrNanoModelConfig() {
        this(null, null, null, null, null, null, 0, 0.0f, 0.0f, 0, null, false, null, 8191, null);
    }

    public static /* synthetic */ com.k2fsa.sherpa.onnx.OfflineFunAsrNanoModelConfig copy$default(com.k2fsa.sherpa.onnx.OfflineFunAsrNanoModelConfig offlineFunAsrNanoModelConfig, java.lang.String str, java.lang.String str2, java.lang.String str3, java.lang.String str4, java.lang.String str5, java.lang.String str6, int i, float f, float f2, int i2, java.lang.String str7, boolean z, java.lang.String str8, int i3, java.lang.Object obj) {
        if ((i3 & 1) != 0) {
            str = offlineFunAsrNanoModelConfig.encoderAdaptor;
        }
        return offlineFunAsrNanoModelConfig.copy(str, (i3 & 2) != 0 ? offlineFunAsrNanoModelConfig.llm : str2, (i3 & 4) != 0 ? offlineFunAsrNanoModelConfig.embedding : str3, (i3 & 8) != 0 ? offlineFunAsrNanoModelConfig.tokenizer : str4, (i3 & 16) != 0 ? offlineFunAsrNanoModelConfig.systemPrompt : str5, (i3 & 32) != 0 ? offlineFunAsrNanoModelConfig.userPrompt : str6, (i3 & 64) != 0 ? offlineFunAsrNanoModelConfig.maxNewTokens : i, (i3 & 128) != 0 ? offlineFunAsrNanoModelConfig.temperature : f, (i3 & io.github.mangi.eta.agent.voice.local.LocalSpeechVoskArchive.MAX_ENTRIES) != 0 ? offlineFunAsrNanoModelConfig.topP : f2, (i3 & io.github.mangi.eta.agent.web.WebPageContent.MAX_TITLE_CHARS) != 0 ? offlineFunAsrNanoModelConfig.seed : i2, (i3 & 1024) != 0 ? offlineFunAsrNanoModelConfig.language : str7, (i3 & 2048) != 0 ? offlineFunAsrNanoModelConfig.itn : z, (i3 & 4096) != 0 ? offlineFunAsrNanoModelConfig.hotwords : str8);
    }

    /* JADX INFO: renamed from: component1, reason: from getter */
    public final java.lang.String getEncoderAdaptor() {
        return this.encoderAdaptor;
    }

    /* JADX INFO: renamed from: component10, reason: from getter */
    public final int getSeed() {
        return this.seed;
    }

    /* JADX INFO: renamed from: component11, reason: from getter */
    public final java.lang.String getLanguage() {
        return this.language;
    }

    /* JADX INFO: renamed from: component12, reason: from getter */
    public final boolean getItn() {
        return this.itn;
    }

    /* JADX INFO: renamed from: component13, reason: from getter */
    public final java.lang.String getHotwords() {
        return this.hotwords;
    }

    /* JADX INFO: renamed from: component2, reason: from getter */
    public final java.lang.String getLlm() {
        return this.llm;
    }

    /* JADX INFO: renamed from: component3, reason: from getter */
    public final java.lang.String getEmbedding() {
        return this.embedding;
    }

    /* JADX INFO: renamed from: component4, reason: from getter */
    public final java.lang.String getTokenizer() {
        return this.tokenizer;
    }

    /* JADX INFO: renamed from: component5, reason: from getter */
    public final java.lang.String getSystemPrompt() {
        return this.systemPrompt;
    }

    /* JADX INFO: renamed from: component6, reason: from getter */
    public final java.lang.String getUserPrompt() {
        return this.userPrompt;
    }

    /* JADX INFO: renamed from: component7, reason: from getter */
    public final int getMaxNewTokens() {
        return this.maxNewTokens;
    }

    /* JADX INFO: renamed from: component8, reason: from getter */
    public final float getTemperature() {
        return this.temperature;
    }

    /* JADX INFO: renamed from: component9, reason: from getter */
    public final float getTopP() {
        return this.topP;
    }

    public final com.k2fsa.sherpa.onnx.OfflineFunAsrNanoModelConfig copy(java.lang.String encoderAdaptor, java.lang.String llm, java.lang.String embedding, java.lang.String tokenizer, java.lang.String systemPrompt, java.lang.String userPrompt, int maxNewTokens, float temperature, float topP, int seed, java.lang.String language, boolean itn, java.lang.String hotwords) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(encoderAdaptor, "encoderAdaptor");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(llm, "llm");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(embedding, "embedding");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(tokenizer, "tokenizer");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(systemPrompt, "systemPrompt");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(userPrompt, "userPrompt");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(language, "language");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(hotwords, "hotwords");
        return new com.k2fsa.sherpa.onnx.OfflineFunAsrNanoModelConfig(encoderAdaptor, llm, embedding, tokenizer, systemPrompt, userPrompt, maxNewTokens, temperature, topP, seed, language, itn, hotwords);
    }

    public boolean equals(java.lang.Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof com.k2fsa.sherpa.onnx.OfflineFunAsrNanoModelConfig)) {
            return false;
        }
        com.k2fsa.sherpa.onnx.OfflineFunAsrNanoModelConfig offlineFunAsrNanoModelConfig = (com.k2fsa.sherpa.onnx.OfflineFunAsrNanoModelConfig) other;
        return kotlin.jvm.internal.Intrinsics.areEqual(this.encoderAdaptor, offlineFunAsrNanoModelConfig.encoderAdaptor) && kotlin.jvm.internal.Intrinsics.areEqual(this.llm, offlineFunAsrNanoModelConfig.llm) && kotlin.jvm.internal.Intrinsics.areEqual(this.embedding, offlineFunAsrNanoModelConfig.embedding) && kotlin.jvm.internal.Intrinsics.areEqual(this.tokenizer, offlineFunAsrNanoModelConfig.tokenizer) && kotlin.jvm.internal.Intrinsics.areEqual(this.systemPrompt, offlineFunAsrNanoModelConfig.systemPrompt) && kotlin.jvm.internal.Intrinsics.areEqual(this.userPrompt, offlineFunAsrNanoModelConfig.userPrompt) && this.maxNewTokens == offlineFunAsrNanoModelConfig.maxNewTokens && java.lang.Float.compare(this.temperature, offlineFunAsrNanoModelConfig.temperature) == 0 && java.lang.Float.compare(this.topP, offlineFunAsrNanoModelConfig.topP) == 0 && this.seed == offlineFunAsrNanoModelConfig.seed && kotlin.jvm.internal.Intrinsics.areEqual(this.language, offlineFunAsrNanoModelConfig.language) && this.itn == offlineFunAsrNanoModelConfig.itn && kotlin.jvm.internal.Intrinsics.areEqual(this.hotwords, offlineFunAsrNanoModelConfig.hotwords);
    }

    public int hashCode() {
        return (((((((((((((((((((((((this.encoderAdaptor.hashCode() * 31) + this.llm.hashCode()) * 31) + this.embedding.hashCode()) * 31) + this.tokenizer.hashCode()) * 31) + this.systemPrompt.hashCode()) * 31) + this.userPrompt.hashCode()) * 31) + java.lang.Integer.hashCode(this.maxNewTokens)) * 31) + java.lang.Float.hashCode(this.temperature)) * 31) + java.lang.Float.hashCode(this.topP)) * 31) + java.lang.Integer.hashCode(this.seed)) * 31) + this.language.hashCode()) * 31) + java.lang.Boolean.hashCode(this.itn)) * 31) + this.hotwords.hashCode();
    }

    public java.lang.String toString() {
        return "OfflineFunAsrNanoModelConfig(encoderAdaptor=" + this.encoderAdaptor + ", llm=" + this.llm + ", embedding=" + this.embedding + ", tokenizer=" + this.tokenizer + ", systemPrompt=" + this.systemPrompt + ", userPrompt=" + this.userPrompt + ", maxNewTokens=" + this.maxNewTokens + ", temperature=" + this.temperature + ", topP=" + this.topP + ", seed=" + this.seed + ", language=" + this.language + ", itn=" + this.itn + ", hotwords=" + this.hotwords + ")";
    }

    public OfflineFunAsrNanoModelConfig(java.lang.String encoderAdaptor, java.lang.String llm, java.lang.String embedding, java.lang.String tokenizer, java.lang.String systemPrompt, java.lang.String userPrompt, int maxNewTokens, float temperature, float topP, int seed, java.lang.String language, boolean itn, java.lang.String hotwords) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(encoderAdaptor, "encoderAdaptor");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(llm, "llm");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(embedding, "embedding");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(tokenizer, "tokenizer");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(systemPrompt, "systemPrompt");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(userPrompt, "userPrompt");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(language, "language");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(hotwords, "hotwords");
        this.encoderAdaptor = encoderAdaptor;
        this.llm = llm;
        this.embedding = embedding;
        this.tokenizer = tokenizer;
        this.systemPrompt = systemPrompt;
        this.userPrompt = userPrompt;
        this.maxNewTokens = maxNewTokens;
        this.temperature = temperature;
        this.topP = topP;
        this.seed = seed;
        this.language = language;
        this.itn = itn;
        this.hotwords = hotwords;
    }

    public /* synthetic */ OfflineFunAsrNanoModelConfig(java.lang.String str, java.lang.String str2, java.lang.String str3, java.lang.String str4, java.lang.String str5, java.lang.String str6, int i, float f, float f2, int i2, java.lang.String str7, boolean z, java.lang.String str8, int i3, kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
        this((i3 & 1) != 0 ? "" : str, (i3 & 2) != 0 ? "" : str2, (i3 & 4) != 0 ? "" : str3, (i3 & 8) != 0 ? "" : str4, (i3 & 16) != 0 ? "You are a helpful assistant." : str5, (i3 & 32) != 0 ? "语音转写：" : str6, (i3 & 64) != 0 ? io.github.mangi.eta.agent.web.WebPageContent.MAX_TITLE_CHARS : i, (i3 & 128) != 0 ? 1.0E-6f : f, (i3 & io.github.mangi.eta.agent.voice.local.LocalSpeechVoskArchive.MAX_ENTRIES) != 0 ? 0.8f : f2, (i3 & io.github.mangi.eta.agent.web.WebPageContent.MAX_TITLE_CHARS) != 0 ? 42 : i2, (i3 & 1024) != 0 ? "" : str7, (i3 & 2048) != 0 ? true : z, (i3 & 4096) == 0 ? str8 : "");
    }

    public final java.lang.String getEncoderAdaptor() {
        return this.encoderAdaptor;
    }

    public final void setEncoderAdaptor(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.encoderAdaptor = str;
    }

    public final java.lang.String getLlm() {
        return this.llm;
    }

    public final void setLlm(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.llm = str;
    }

    public final java.lang.String getEmbedding() {
        return this.embedding;
    }

    public final void setEmbedding(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.embedding = str;
    }

    public final java.lang.String getTokenizer() {
        return this.tokenizer;
    }

    public final void setTokenizer(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.tokenizer = str;
    }

    public final java.lang.String getSystemPrompt() {
        return this.systemPrompt;
    }

    public final void setSystemPrompt(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.systemPrompt = str;
    }

    public final java.lang.String getUserPrompt() {
        return this.userPrompt;
    }

    public final void setUserPrompt(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.userPrompt = str;
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

    public final java.lang.String getLanguage() {
        return this.language;
    }

    public final void setLanguage(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.language = str;
    }

    public final boolean getItn() {
        return this.itn;
    }

    public final void setItn(boolean z) {
        this.itn = z;
    }

    public final java.lang.String getHotwords() {
        return this.hotwords;
    }

    public final void setHotwords(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.hotwords = str;
    }
}
