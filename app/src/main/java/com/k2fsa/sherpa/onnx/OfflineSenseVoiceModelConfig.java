package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: OfflineRecognizer.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0018\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B/\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b¢\u0006\u0004\b\t\u0010\nJ\t\u0010\u0019\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001a\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001b\u001a\u00020\u0006HÆ\u0003J\t\u0010\u001c\u001a\u00020\bHÆ\u0003J1\u0010\u001d\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\bHÆ\u0001J\u0014\u0010\u001e\u001a\u00020\u00062\b\u0010\u001f\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010 \u001a\u00020!HÖ\u0081\u0004J\n\u0010\"\u001a\u00020\u0003HÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u001a\u0010\u0004\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000f\u0010\f\"\u0004\b\u0010\u0010\u000eR\u001a\u0010\u0005\u001a\u00020\u0006X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R\u001a\u0010\u0007\u001a\u00020\bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0015\u0010\u0016\"\u0004\b\u0017\u0010\u0018Ê\u0001\f\b$\u0012\b\b%\u0012\u0004\b\u0003\u0010\u0000¨\u0006#"}, d2 = {"Lcom/k2fsa/sherpa/onnx/OfflineSenseVoiceModelConfig;", "", "model", "", "language", "useInverseTextNormalization", "", "qnnConfig", "Lcom/k2fsa/sherpa/onnx/QnnConfig;", "<init>", "(Ljava/lang/String;Ljava/lang/String;ZLcom/k2fsa/sherpa/onnx/QnnConfig;)V", "getModel", "()Ljava/lang/String;", "setModel", "(Ljava/lang/String;)V", "getLanguage", "setLanguage", "getUseInverseTextNormalization", "()Z", "setUseInverseTextNormalization", "(Z)V", "getQnnConfig", "()Lcom/k2fsa/sherpa/onnx/QnnConfig;", "setQnnConfig", "(Lcom/k2fsa/sherpa/onnx/QnnConfig;)V", "component1", "component2", "component3", "component4", "copy", "equals", "other", "hashCode", "", "toString", "app", "Landroidx/compose/runtime/internal/StabilityInferred;", "parameters"}, k = 1, mv = {2, 4, 0}, xi = 48)
public final /* data */ class OfflineSenseVoiceModelConfig {
    public static final int $stable = 8;
    private java.lang.String language;
    private java.lang.String model;
    private com.k2fsa.sherpa.onnx.QnnConfig qnnConfig;
    private boolean useInverseTextNormalization;

    public OfflineSenseVoiceModelConfig() {
        this(null, null, false, null, 15, null);
    }

    public static /* synthetic */ com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig copy$default(com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig offlineSenseVoiceModelConfig, java.lang.String str, java.lang.String str2, boolean z, com.k2fsa.sherpa.onnx.QnnConfig qnnConfig, int i, java.lang.Object obj) {
        if ((i & 1) != 0) {
            str = offlineSenseVoiceModelConfig.model;
        }
        if ((i & 2) != 0) {
            str2 = offlineSenseVoiceModelConfig.language;
        }
        if ((i & 4) != 0) {
            z = offlineSenseVoiceModelConfig.useInverseTextNormalization;
        }
        if ((i & 8) != 0) {
            qnnConfig = offlineSenseVoiceModelConfig.qnnConfig;
        }
        return offlineSenseVoiceModelConfig.copy(str, str2, z, qnnConfig);
    }

    /* JADX INFO: renamed from: component1, reason: from getter */
    public final java.lang.String getModel() {
        return this.model;
    }

    /* JADX INFO: renamed from: component2, reason: from getter */
    public final java.lang.String getLanguage() {
        return this.language;
    }

    /* JADX INFO: renamed from: component3, reason: from getter */
    public final boolean getUseInverseTextNormalization() {
        return this.useInverseTextNormalization;
    }

    /* JADX INFO: renamed from: component4, reason: from getter */
    public final com.k2fsa.sherpa.onnx.QnnConfig getQnnConfig() {
        return this.qnnConfig;
    }

    public final com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig copy(java.lang.String model, java.lang.String language, boolean useInverseTextNormalization, com.k2fsa.sherpa.onnx.QnnConfig qnnConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(model, "model");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(language, "language");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(qnnConfig, "qnnConfig");
        return new com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig(model, language, useInverseTextNormalization, qnnConfig);
    }

    public boolean equals(java.lang.Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig)) {
            return false;
        }
        com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig offlineSenseVoiceModelConfig = (com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig) other;
        return kotlin.jvm.internal.Intrinsics.areEqual(this.model, offlineSenseVoiceModelConfig.model) && kotlin.jvm.internal.Intrinsics.areEqual(this.language, offlineSenseVoiceModelConfig.language) && this.useInverseTextNormalization == offlineSenseVoiceModelConfig.useInverseTextNormalization && kotlin.jvm.internal.Intrinsics.areEqual(this.qnnConfig, offlineSenseVoiceModelConfig.qnnConfig);
    }

    public int hashCode() {
        return (((((this.model.hashCode() * 31) + this.language.hashCode()) * 31) + java.lang.Boolean.hashCode(this.useInverseTextNormalization)) * 31) + this.qnnConfig.hashCode();
    }

    public java.lang.String toString() {
        return "OfflineSenseVoiceModelConfig(model=" + this.model + ", language=" + this.language + ", useInverseTextNormalization=" + this.useInverseTextNormalization + ", qnnConfig=" + this.qnnConfig + ")";
    }

    public OfflineSenseVoiceModelConfig(java.lang.String model, java.lang.String language, boolean useInverseTextNormalization, com.k2fsa.sherpa.onnx.QnnConfig qnnConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(model, "model");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(language, "language");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(qnnConfig, "qnnConfig");
        this.model = model;
        this.language = language;
        this.useInverseTextNormalization = useInverseTextNormalization;
        this.qnnConfig = qnnConfig;
    }

    public /* synthetic */ OfflineSenseVoiceModelConfig(java.lang.String str, java.lang.String str2, boolean z, com.k2fsa.sherpa.onnx.QnnConfig qnnConfig, int i, kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
        this((i & 1) != 0 ? "" : str, (i & 2) != 0 ? "" : str2, (i & 4) != 0 ? true : z, (i & 8) != 0 ? new com.k2fsa.sherpa.onnx.QnnConfig(null, null, null, 7, null) : qnnConfig);
    }

    public final java.lang.String getModel() {
        return this.model;
    }

    public final void setModel(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.model = str;
    }

    public final java.lang.String getLanguage() {
        return this.language;
    }

    public final void setLanguage(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.language = str;
    }

    public final boolean getUseInverseTextNormalization() {
        return this.useInverseTextNormalization;
    }

    public final void setUseInverseTextNormalization(boolean z) {
        this.useInverseTextNormalization = z;
    }

    public final com.k2fsa.sherpa.onnx.QnnConfig getQnnConfig() {
        return this.qnnConfig;
    }

    public final void setQnnConfig(com.k2fsa.sherpa.onnx.QnnConfig qnnConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(qnnConfig, "<set-?>");
        this.qnnConfig = qnnConfig;
    }
}
