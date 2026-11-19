package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: FeatureConfig.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0011\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B%\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006¢\u0006\u0004\b\u0007\u0010\bJ\t\u0010\u0013\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0014\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0015\u001a\u00020\u0006HÆ\u0003J'\u0010\u0016\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0006HÆ\u0001J\u0014\u0010\u0017\u001a\u00020\u00182\b\u0010\u0019\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u001a\u001a\u00020\u0003HÖ\u0081\u0004J\n\u0010\u001b\u001a\u00020\u001cHÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\fR\u001a\u0010\u0004\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\n\"\u0004\b\u000e\u0010\fR\u001a\u0010\u0005\u001a\u00020\u0006X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000f\u0010\u0010\"\u0004\b\u0011\u0010\u0012Ê\u0001\f\b\u001e\u0012\b\b\u001f\u0012\u0004\b\u0003\u0010\u0000¨\u0006\u001d"}, d2 = {"Lcom/k2fsa/sherpa/onnx/FeatureConfig;", "", "sampleRate", "", "featureDim", "dither", "", "<init>", "(IIF)V", "getSampleRate", "()I", "setSampleRate", "(I)V", "getFeatureDim", "setFeatureDim", "getDither", "()F", "setDither", "(F)V", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "toString", "", "app", "Landroidx/compose/runtime/internal/StabilityInferred;", "parameters"}, k = 1, mv = {2, 4, 0}, xi = 48)
public final /* data */ class FeatureConfig {
    public static final int $stable = 8;
    private float dither;
    private int featureDim;
    private int sampleRate;

    public FeatureConfig() {
        this(0, 0, 0.0f, 7, null);
    }

    public static /* synthetic */ com.k2fsa.sherpa.onnx.FeatureConfig copy$default(com.k2fsa.sherpa.onnx.FeatureConfig featureConfig, int i, int i2, float f, int i3, java.lang.Object obj) {
        if ((i3 & 1) != 0) {
            i = featureConfig.sampleRate;
        }
        if ((i3 & 2) != 0) {
            i2 = featureConfig.featureDim;
        }
        if ((i3 & 4) != 0) {
            f = featureConfig.dither;
        }
        return featureConfig.copy(i, i2, f);
    }

    /* JADX INFO: renamed from: component1, reason: from getter */
    public final int getSampleRate() {
        return this.sampleRate;
    }

    /* JADX INFO: renamed from: component2, reason: from getter */
    public final int getFeatureDim() {
        return this.featureDim;
    }

    /* JADX INFO: renamed from: component3, reason: from getter */
    public final float getDither() {
        return this.dither;
    }

    public final com.k2fsa.sherpa.onnx.FeatureConfig copy(int sampleRate, int featureDim, float dither) {
        return new com.k2fsa.sherpa.onnx.FeatureConfig(sampleRate, featureDim, dither);
    }

    public boolean equals(java.lang.Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof com.k2fsa.sherpa.onnx.FeatureConfig)) {
            return false;
        }
        com.k2fsa.sherpa.onnx.FeatureConfig featureConfig = (com.k2fsa.sherpa.onnx.FeatureConfig) other;
        return this.sampleRate == featureConfig.sampleRate && this.featureDim == featureConfig.featureDim && java.lang.Float.compare(this.dither, featureConfig.dither) == 0;
    }

    public int hashCode() {
        return (((java.lang.Integer.hashCode(this.sampleRate) * 31) + java.lang.Integer.hashCode(this.featureDim)) * 31) + java.lang.Float.hashCode(this.dither);
    }

    public java.lang.String toString() {
        return "FeatureConfig(sampleRate=" + this.sampleRate + ", featureDim=" + this.featureDim + ", dither=" + this.dither + ")";
    }

    public FeatureConfig(int sampleRate, int featureDim, float dither) {
        this.sampleRate = sampleRate;
        this.featureDim = featureDim;
        this.dither = dither;
    }

    public /* synthetic */ FeatureConfig(int i, int i2, float f, int i3, kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
        this((i3 & 1) != 0 ? io.github.mangi.eta.agent.web.WebPageContent.MAX_PAGE_CHARS : i, (i3 & 2) != 0 ? 80 : i2, (i3 & 4) != 0 ? 0.0f : f);
    }

    public final int getSampleRate() {
        return this.sampleRate;
    }

    public final void setSampleRate(int i) {
        this.sampleRate = i;
    }

    public final int getFeatureDim() {
        return this.featureDim;
    }

    public final void setFeatureDim(int i) {
        this.featureDim = i;
    }

    public final float getDither() {
        return this.dither;
    }

    public final void setDither(float f) {
        this.dither = f;
    }
}
