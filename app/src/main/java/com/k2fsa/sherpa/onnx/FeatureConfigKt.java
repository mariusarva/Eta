package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: FeatureConfig.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u0000\u0010\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\u001a\u0016\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0003¨\u0006\u0005"}, d2 = {"getFeatureConfig", "Lcom/k2fsa/sherpa/onnx/FeatureConfig;", "sampleRate", "", "featureDim", "app"}, k = 2, mv = {2, 4, 0}, xi = 48)
public final class FeatureConfigKt {
    public static final com.k2fsa.sherpa.onnx.FeatureConfig getFeatureConfig(int sampleRate, int featureDim) {
        return new com.k2fsa.sherpa.onnx.FeatureConfig(sampleRate, featureDim, 0.0f, 4, null);
    }
}
