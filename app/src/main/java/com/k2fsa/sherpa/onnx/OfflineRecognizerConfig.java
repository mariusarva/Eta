package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: OfflineRecognizer.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b3\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001Bu\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\t\u0012\b\b\u0002\u0010\r\u001a\u00020\u000e\u0012\b\b\u0002\u0010\u000f\u001a\u00020\t\u0012\b\b\u0002\u0010\u0010\u001a\u00020\t\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u000e\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u0013¢\u0006\u0004\b\u0014\u0010\u0015J\t\u0010:\u001a\u00020\u0003HÆ\u0003J\t\u0010;\u001a\u00020\u0005HÆ\u0003J\t\u0010<\u001a\u00020\u0007HÆ\u0003J\t\u0010=\u001a\u00020\tHÆ\u0003J\t\u0010>\u001a\u00020\u000bHÆ\u0003J\t\u0010?\u001a\u00020\tHÆ\u0003J\t\u0010@\u001a\u00020\u000eHÆ\u0003J\t\u0010A\u001a\u00020\tHÆ\u0003J\t\u0010B\u001a\u00020\tHÆ\u0003J\t\u0010C\u001a\u00020\u000eHÆ\u0003J\t\u0010D\u001a\u00020\u0013HÆ\u0003Jw\u0010E\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\t2\b\b\u0002\u0010\r\u001a\u00020\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\t2\b\b\u0002\u0010\u0010\u001a\u00020\t2\b\b\u0002\u0010\u0011\u001a\u00020\u000e2\b\b\u0002\u0010\u0012\u001a\u00020\u0013HÆ\u0001J\u0014\u0010F\u001a\u00020G2\b\u0010H\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010I\u001a\u00020\u000bHÖ\u0081\u0004J\n\u0010J\u001a\u00020\tHÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\u0017\"\u0004\b\u0018\u0010\u0019R\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001a\u0010\u001b\"\u0004\b\u001c\u0010\u001dR\u001a\u0010\u0006\u001a\u00020\u0007X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001e\u0010\u001f\"\u0004\b \u0010!R\u001a\u0010\b\u001a\u00020\tX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\"\u0010#\"\u0004\b$\u0010%R\u001a\u0010\n\u001a\u00020\u000bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b&\u0010'\"\u0004\b(\u0010)R\u001a\u0010\f\u001a\u00020\tX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b*\u0010#\"\u0004\b+\u0010%R\u001a\u0010\r\u001a\u00020\u000eX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b,\u0010-\"\u0004\b.\u0010/R\u001a\u0010\u000f\u001a\u00020\tX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b0\u0010#\"\u0004\b1\u0010%R\u001a\u0010\u0010\u001a\u00020\tX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b2\u0010#\"\u0004\b3\u0010%R\u001a\u0010\u0011\u001a\u00020\u000eX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b4\u0010-\"\u0004\b5\u0010/R\u001a\u0010\u0012\u001a\u00020\u0013X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b6\u00107\"\u0004\b8\u00109Ê\u0001\f\bL\u0012\b\bM\u0012\u0004\b\u0003\u0010\u0000¨\u0006K"}, d2 = {"Lcom/k2fsa/sherpa/onnx/OfflineRecognizerConfig;", "", "featConfig", "Lcom/k2fsa/sherpa/onnx/FeatureConfig;", "modelConfig", "Lcom/k2fsa/sherpa/onnx/OfflineModelConfig;", "hr", "Lcom/k2fsa/sherpa/onnx/HomophoneReplacerConfig;", "decodingMethod", "", "maxActivePaths", "", "hotwordsFile", "hotwordsScore", "", "ruleFsts", "ruleFars", "blankPenalty", "ctcFstDecoderConfig", "Lcom/k2fsa/sherpa/onnx/OfflineCtcFstDecoderConfig;", "<init>", "(Lcom/k2fsa/sherpa/onnx/FeatureConfig;Lcom/k2fsa/sherpa/onnx/OfflineModelConfig;Lcom/k2fsa/sherpa/onnx/HomophoneReplacerConfig;Ljava/lang/String;ILjava/lang/String;FLjava/lang/String;Ljava/lang/String;FLcom/k2fsa/sherpa/onnx/OfflineCtcFstDecoderConfig;)V", "getFeatConfig", "()Lcom/k2fsa/sherpa/onnx/FeatureConfig;", "setFeatConfig", "(Lcom/k2fsa/sherpa/onnx/FeatureConfig;)V", "getModelConfig", "()Lcom/k2fsa/sherpa/onnx/OfflineModelConfig;", "setModelConfig", "(Lcom/k2fsa/sherpa/onnx/OfflineModelConfig;)V", "getHr", "()Lcom/k2fsa/sherpa/onnx/HomophoneReplacerConfig;", "setHr", "(Lcom/k2fsa/sherpa/onnx/HomophoneReplacerConfig;)V", "getDecodingMethod", "()Ljava/lang/String;", "setDecodingMethod", "(Ljava/lang/String;)V", "getMaxActivePaths", "()I", "setMaxActivePaths", "(I)V", "getHotwordsFile", "setHotwordsFile", "getHotwordsScore", "()F", "setHotwordsScore", "(F)V", "getRuleFsts", "setRuleFsts", "getRuleFars", "setRuleFars", "getBlankPenalty", "setBlankPenalty", "getCtcFstDecoderConfig", "()Lcom/k2fsa/sherpa/onnx/OfflineCtcFstDecoderConfig;", "setCtcFstDecoderConfig", "(Lcom/k2fsa/sherpa/onnx/OfflineCtcFstDecoderConfig;)V", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "component11", "copy", "equals", "", "other", "hashCode", "toString", "app", "Landroidx/compose/runtime/internal/StabilityInferred;", "parameters"}, k = 1, mv = {2, 4, 0}, xi = 48)
public final /* data */ class OfflineRecognizerConfig {
    public static final int $stable = 8;
    private float blankPenalty;
    private com.k2fsa.sherpa.onnx.OfflineCtcFstDecoderConfig ctcFstDecoderConfig;
    private java.lang.String decodingMethod;
    private com.k2fsa.sherpa.onnx.FeatureConfig featConfig;
    private java.lang.String hotwordsFile;
    private float hotwordsScore;
    private com.k2fsa.sherpa.onnx.HomophoneReplacerConfig hr;
    private int maxActivePaths;
    private com.k2fsa.sherpa.onnx.OfflineModelConfig modelConfig;
    private java.lang.String ruleFars;
    private java.lang.String ruleFsts;

    public OfflineRecognizerConfig() {
        this(null, null, null, null, 0, null, 0.0f, null, null, 0.0f, null, 2047, null);
    }

    public static /* synthetic */ com.k2fsa.sherpa.onnx.OfflineRecognizerConfig copy$default(com.k2fsa.sherpa.onnx.OfflineRecognizerConfig offlineRecognizerConfig, com.k2fsa.sherpa.onnx.FeatureConfig featureConfig, com.k2fsa.sherpa.onnx.OfflineModelConfig offlineModelConfig, com.k2fsa.sherpa.onnx.HomophoneReplacerConfig homophoneReplacerConfig, java.lang.String str, int i, java.lang.String str2, float f, java.lang.String str3, java.lang.String str4, float f2, com.k2fsa.sherpa.onnx.OfflineCtcFstDecoderConfig offlineCtcFstDecoderConfig, int i2, java.lang.Object obj) {
        if ((i2 & 1) != 0) {
            featureConfig = offlineRecognizerConfig.featConfig;
        }
        if ((i2 & 2) != 0) {
            offlineModelConfig = offlineRecognizerConfig.modelConfig;
        }
        if ((i2 & 4) != 0) {
            homophoneReplacerConfig = offlineRecognizerConfig.hr;
        }
        if ((i2 & 8) != 0) {
            str = offlineRecognizerConfig.decodingMethod;
        }
        if ((i2 & 16) != 0) {
            i = offlineRecognizerConfig.maxActivePaths;
        }
        if ((i2 & 32) != 0) {
            str2 = offlineRecognizerConfig.hotwordsFile;
        }
        if ((i2 & 64) != 0) {
            f = offlineRecognizerConfig.hotwordsScore;
        }
        if ((i2 & 128) != 0) {
            str3 = offlineRecognizerConfig.ruleFsts;
        }
        if ((i2 & io.github.mangi.eta.agent.voice.local.LocalSpeechVoskArchive.MAX_ENTRIES) != 0) {
            str4 = offlineRecognizerConfig.ruleFars;
        }
        if ((i2 & io.github.mangi.eta.agent.web.WebPageContent.MAX_TITLE_CHARS) != 0) {
            f2 = offlineRecognizerConfig.blankPenalty;
        }
        if ((i2 & 1024) != 0) {
            offlineCtcFstDecoderConfig = offlineRecognizerConfig.ctcFstDecoderConfig;
        }
        float f3 = f2;
        com.k2fsa.sherpa.onnx.OfflineCtcFstDecoderConfig offlineCtcFstDecoderConfig2 = offlineCtcFstDecoderConfig;
        java.lang.String str5 = str3;
        java.lang.String str6 = str4;
        java.lang.String str7 = str2;
        float f4 = f;
        int i3 = i;
        com.k2fsa.sherpa.onnx.HomophoneReplacerConfig homophoneReplacerConfig2 = homophoneReplacerConfig;
        return offlineRecognizerConfig.copy(featureConfig, offlineModelConfig, homophoneReplacerConfig2, str, i3, str7, f4, str5, str6, f3, offlineCtcFstDecoderConfig2);
    }

    /* JADX INFO: renamed from: component1, reason: from getter */
    public final com.k2fsa.sherpa.onnx.FeatureConfig getFeatConfig() {
        return this.featConfig;
    }

    /* JADX INFO: renamed from: component10, reason: from getter */
    public final float getBlankPenalty() {
        return this.blankPenalty;
    }

    /* JADX INFO: renamed from: component11, reason: from getter */
    public final com.k2fsa.sherpa.onnx.OfflineCtcFstDecoderConfig getCtcFstDecoderConfig() {
        return this.ctcFstDecoderConfig;
    }

    /* JADX INFO: renamed from: component2, reason: from getter */
    public final com.k2fsa.sherpa.onnx.OfflineModelConfig getModelConfig() {
        return this.modelConfig;
    }

    /* JADX INFO: renamed from: component3, reason: from getter */
    public final com.k2fsa.sherpa.onnx.HomophoneReplacerConfig getHr() {
        return this.hr;
    }

    /* JADX INFO: renamed from: component4, reason: from getter */
    public final java.lang.String getDecodingMethod() {
        return this.decodingMethod;
    }

    /* JADX INFO: renamed from: component5, reason: from getter */
    public final int getMaxActivePaths() {
        return this.maxActivePaths;
    }

    /* JADX INFO: renamed from: component6, reason: from getter */
    public final java.lang.String getHotwordsFile() {
        return this.hotwordsFile;
    }

    /* JADX INFO: renamed from: component7, reason: from getter */
    public final float getHotwordsScore() {
        return this.hotwordsScore;
    }

    /* JADX INFO: renamed from: component8, reason: from getter */
    public final java.lang.String getRuleFsts() {
        return this.ruleFsts;
    }

    /* JADX INFO: renamed from: component9, reason: from getter */
    public final java.lang.String getRuleFars() {
        return this.ruleFars;
    }

    public final com.k2fsa.sherpa.onnx.OfflineRecognizerConfig copy(com.k2fsa.sherpa.onnx.FeatureConfig featConfig, com.k2fsa.sherpa.onnx.OfflineModelConfig modelConfig, com.k2fsa.sherpa.onnx.HomophoneReplacerConfig hr, java.lang.String decodingMethod, int maxActivePaths, java.lang.String hotwordsFile, float hotwordsScore, java.lang.String ruleFsts, java.lang.String ruleFars, float blankPenalty, com.k2fsa.sherpa.onnx.OfflineCtcFstDecoderConfig ctcFstDecoderConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(featConfig, "featConfig");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(modelConfig, "modelConfig");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(hr, "hr");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(decodingMethod, "decodingMethod");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(hotwordsFile, "hotwordsFile");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(ruleFsts, "ruleFsts");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(ruleFars, "ruleFars");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(ctcFstDecoderConfig, "ctcFstDecoderConfig");
        return new com.k2fsa.sherpa.onnx.OfflineRecognizerConfig(featConfig, modelConfig, hr, decodingMethod, maxActivePaths, hotwordsFile, hotwordsScore, ruleFsts, ruleFars, blankPenalty, ctcFstDecoderConfig);
    }

    public boolean equals(java.lang.Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof com.k2fsa.sherpa.onnx.OfflineRecognizerConfig)) {
            return false;
        }
        com.k2fsa.sherpa.onnx.OfflineRecognizerConfig offlineRecognizerConfig = (com.k2fsa.sherpa.onnx.OfflineRecognizerConfig) other;
        return kotlin.jvm.internal.Intrinsics.areEqual(this.featConfig, offlineRecognizerConfig.featConfig) && kotlin.jvm.internal.Intrinsics.areEqual(this.modelConfig, offlineRecognizerConfig.modelConfig) && kotlin.jvm.internal.Intrinsics.areEqual(this.hr, offlineRecognizerConfig.hr) && kotlin.jvm.internal.Intrinsics.areEqual(this.decodingMethod, offlineRecognizerConfig.decodingMethod) && this.maxActivePaths == offlineRecognizerConfig.maxActivePaths && kotlin.jvm.internal.Intrinsics.areEqual(this.hotwordsFile, offlineRecognizerConfig.hotwordsFile) && java.lang.Float.compare(this.hotwordsScore, offlineRecognizerConfig.hotwordsScore) == 0 && kotlin.jvm.internal.Intrinsics.areEqual(this.ruleFsts, offlineRecognizerConfig.ruleFsts) && kotlin.jvm.internal.Intrinsics.areEqual(this.ruleFars, offlineRecognizerConfig.ruleFars) && java.lang.Float.compare(this.blankPenalty, offlineRecognizerConfig.blankPenalty) == 0 && kotlin.jvm.internal.Intrinsics.areEqual(this.ctcFstDecoderConfig, offlineRecognizerConfig.ctcFstDecoderConfig);
    }

    public int hashCode() {
        return (((((((((((((((((((this.featConfig.hashCode() * 31) + this.modelConfig.hashCode()) * 31) + this.hr.hashCode()) * 31) + this.decodingMethod.hashCode()) * 31) + java.lang.Integer.hashCode(this.maxActivePaths)) * 31) + this.hotwordsFile.hashCode()) * 31) + java.lang.Float.hashCode(this.hotwordsScore)) * 31) + this.ruleFsts.hashCode()) * 31) + this.ruleFars.hashCode()) * 31) + java.lang.Float.hashCode(this.blankPenalty)) * 31) + this.ctcFstDecoderConfig.hashCode();
    }

    public java.lang.String toString() {
        return "OfflineRecognizerConfig(featConfig=" + this.featConfig + ", modelConfig=" + this.modelConfig + ", hr=" + this.hr + ", decodingMethod=" + this.decodingMethod + ", maxActivePaths=" + this.maxActivePaths + ", hotwordsFile=" + this.hotwordsFile + ", hotwordsScore=" + this.hotwordsScore + ", ruleFsts=" + this.ruleFsts + ", ruleFars=" + this.ruleFars + ", blankPenalty=" + this.blankPenalty + ", ctcFstDecoderConfig=" + this.ctcFstDecoderConfig + ")";
    }

    public OfflineRecognizerConfig(com.k2fsa.sherpa.onnx.FeatureConfig featConfig, com.k2fsa.sherpa.onnx.OfflineModelConfig modelConfig, com.k2fsa.sherpa.onnx.HomophoneReplacerConfig hr, java.lang.String decodingMethod, int maxActivePaths, java.lang.String hotwordsFile, float hotwordsScore, java.lang.String ruleFsts, java.lang.String ruleFars, float blankPenalty, com.k2fsa.sherpa.onnx.OfflineCtcFstDecoderConfig ctcFstDecoderConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(featConfig, "featConfig");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(modelConfig, "modelConfig");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(hr, "hr");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(decodingMethod, "decodingMethod");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(hotwordsFile, "hotwordsFile");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(ruleFsts, "ruleFsts");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(ruleFars, "ruleFars");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(ctcFstDecoderConfig, "ctcFstDecoderConfig");
        this.featConfig = featConfig;
        this.modelConfig = modelConfig;
        this.hr = hr;
        this.decodingMethod = decodingMethod;
        this.maxActivePaths = maxActivePaths;
        this.hotwordsFile = hotwordsFile;
        this.hotwordsScore = hotwordsScore;
        this.ruleFsts = ruleFsts;
        this.ruleFars = ruleFars;
        this.blankPenalty = blankPenalty;
        this.ctcFstDecoderConfig = ctcFstDecoderConfig;
    }

    public /* synthetic */ OfflineRecognizerConfig(com.k2fsa.sherpa.onnx.FeatureConfig featureConfig, com.k2fsa.sherpa.onnx.OfflineModelConfig offlineModelConfig, com.k2fsa.sherpa.onnx.HomophoneReplacerConfig homophoneReplacerConfig, java.lang.String str, int i, java.lang.String str2, float f, java.lang.String str3, java.lang.String str4, float f2, com.k2fsa.sherpa.onnx.OfflineCtcFstDecoderConfig offlineCtcFstDecoderConfig, int i2, kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
        this((i2 & 1) != 0 ? new com.k2fsa.sherpa.onnx.FeatureConfig(0, 0, 0.0f, 7, null) : featureConfig, (i2 & 2) != 0 ? new com.k2fsa.sherpa.onnx.OfflineModelConfig(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 0, false, null, null, null, null, null, 33554431, null) : offlineModelConfig, (i2 & 4) != 0 ? new com.k2fsa.sherpa.onnx.HomophoneReplacerConfig(null, null, null, 7, null) : homophoneReplacerConfig, (i2 & 8) != 0 ? "greedy_search" : str, (i2 & 16) != 0 ? 4 : i, (i2 & 32) != 0 ? "" : str2, (i2 & 64) != 0 ? 1.5f : f, (i2 & 128) != 0 ? "" : str3, (i2 & io.github.mangi.eta.agent.voice.local.LocalSpeechVoskArchive.MAX_ENTRIES) == 0 ? str4 : "", (i2 & io.github.mangi.eta.agent.web.WebPageContent.MAX_TITLE_CHARS) != 0 ? 0.0f : f2, (i2 & 1024) != 0 ? new com.k2fsa.sherpa.onnx.OfflineCtcFstDecoderConfig(null, 0, 3, null) : offlineCtcFstDecoderConfig);
    }

    public final com.k2fsa.sherpa.onnx.FeatureConfig getFeatConfig() {
        return this.featConfig;
    }

    public final void setFeatConfig(com.k2fsa.sherpa.onnx.FeatureConfig featureConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(featureConfig, "<set-?>");
        this.featConfig = featureConfig;
    }

    public final com.k2fsa.sherpa.onnx.OfflineModelConfig getModelConfig() {
        return this.modelConfig;
    }

    public final void setModelConfig(com.k2fsa.sherpa.onnx.OfflineModelConfig offlineModelConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(offlineModelConfig, "<set-?>");
        this.modelConfig = offlineModelConfig;
    }

    public final com.k2fsa.sherpa.onnx.HomophoneReplacerConfig getHr() {
        return this.hr;
    }

    public final void setHr(com.k2fsa.sherpa.onnx.HomophoneReplacerConfig homophoneReplacerConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(homophoneReplacerConfig, "<set-?>");
        this.hr = homophoneReplacerConfig;
    }

    public final java.lang.String getDecodingMethod() {
        return this.decodingMethod;
    }

    public final void setDecodingMethod(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.decodingMethod = str;
    }

    public final int getMaxActivePaths() {
        return this.maxActivePaths;
    }

    public final void setMaxActivePaths(int i) {
        this.maxActivePaths = i;
    }

    public final java.lang.String getHotwordsFile() {
        return this.hotwordsFile;
    }

    public final void setHotwordsFile(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.hotwordsFile = str;
    }

    public final float getHotwordsScore() {
        return this.hotwordsScore;
    }

    public final void setHotwordsScore(float f) {
        this.hotwordsScore = f;
    }

    public final java.lang.String getRuleFsts() {
        return this.ruleFsts;
    }

    public final void setRuleFsts(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.ruleFsts = str;
    }

    public final java.lang.String getRuleFars() {
        return this.ruleFars;
    }

    public final void setRuleFars(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.ruleFars = str;
    }

    public final float getBlankPenalty() {
        return this.blankPenalty;
    }

    public final void setBlankPenalty(float f) {
        this.blankPenalty = f;
    }

    public final com.k2fsa.sherpa.onnx.OfflineCtcFstDecoderConfig getCtcFstDecoderConfig() {
        return this.ctcFstDecoderConfig;
    }

    public final void setCtcFstDecoderConfig(com.k2fsa.sherpa.onnx.OfflineCtcFstDecoderConfig offlineCtcFstDecoderConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(offlineCtcFstDecoderConfig, "<set-?>");
        this.ctcFstDecoderConfig = offlineCtcFstDecoderConfig;
    }
}
