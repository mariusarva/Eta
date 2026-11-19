package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: OfflineRecognizer.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0019\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B9\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0003¢\u0006\u0004\b\b\u0010\tJ\t\u0010\u0016\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0017\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0018\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0019\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001a\u001a\u00020\u0003HÆ\u0003J;\u0010\u001b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u0003HÆ\u0001J\u0014\u0010\u001c\u001a\u00020\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u001f\u001a\u00020 HÖ\u0081\u0004J\n\u0010!\u001a\u00020\u0003HÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\rR\u001a\u0010\u0004\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u000b\"\u0004\b\u000f\u0010\rR\u001a\u0010\u0005\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u000b\"\u0004\b\u0011\u0010\rR\u001a\u0010\u0006\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u000b\"\u0004\b\u0013\u0010\rR\u001a\u0010\u0007\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0014\u0010\u000b\"\u0004\b\u0015\u0010\rÊ\u0001\f\b#\u0012\b\b$\u0012\u0004\b\u0003\u0010\u0000¨\u0006\""}, d2 = {"Lcom/k2fsa/sherpa/onnx/OfflineMoonshineModelConfig;", "", "preprocessor", "", "encoder", "uncachedDecoder", "cachedDecoder", "mergedDecoder", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getPreprocessor", "()Ljava/lang/String;", "setPreprocessor", "(Ljava/lang/String;)V", "getEncoder", "setEncoder", "getUncachedDecoder", "setUncachedDecoder", "getCachedDecoder", "setCachedDecoder", "getMergedDecoder", "setMergedDecoder", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "", "other", "hashCode", "", "toString", "app", "Landroidx/compose/runtime/internal/StabilityInferred;", "parameters"}, k = 1, mv = {2, 4, 0}, xi = 48)
public final /* data */ class OfflineMoonshineModelConfig {
    public static final int $stable = 8;
    private java.lang.String cachedDecoder;
    private java.lang.String encoder;
    private java.lang.String mergedDecoder;
    private java.lang.String preprocessor;
    private java.lang.String uncachedDecoder;

    public OfflineMoonshineModelConfig() {
        this(null, null, null, null, null, 31, null);
    }

    public static /* synthetic */ com.k2fsa.sherpa.onnx.OfflineMoonshineModelConfig copy$default(com.k2fsa.sherpa.onnx.OfflineMoonshineModelConfig offlineMoonshineModelConfig, java.lang.String str, java.lang.String str2, java.lang.String str3, java.lang.String str4, java.lang.String str5, int i, java.lang.Object obj) {
        if ((i & 1) != 0) {
            str = offlineMoonshineModelConfig.preprocessor;
        }
        if ((i & 2) != 0) {
            str2 = offlineMoonshineModelConfig.encoder;
        }
        if ((i & 4) != 0) {
            str3 = offlineMoonshineModelConfig.uncachedDecoder;
        }
        if ((i & 8) != 0) {
            str4 = offlineMoonshineModelConfig.cachedDecoder;
        }
        if ((i & 16) != 0) {
            str5 = offlineMoonshineModelConfig.mergedDecoder;
        }
        java.lang.String str6 = str5;
        java.lang.String str7 = str3;
        return offlineMoonshineModelConfig.copy(str, str2, str7, str4, str6);
    }

    /* JADX INFO: renamed from: component1, reason: from getter */
    public final java.lang.String getPreprocessor() {
        return this.preprocessor;
    }

    /* JADX INFO: renamed from: component2, reason: from getter */
    public final java.lang.String getEncoder() {
        return this.encoder;
    }

    /* JADX INFO: renamed from: component3, reason: from getter */
    public final java.lang.String getUncachedDecoder() {
        return this.uncachedDecoder;
    }

    /* JADX INFO: renamed from: component4, reason: from getter */
    public final java.lang.String getCachedDecoder() {
        return this.cachedDecoder;
    }

    /* JADX INFO: renamed from: component5, reason: from getter */
    public final java.lang.String getMergedDecoder() {
        return this.mergedDecoder;
    }

    public final com.k2fsa.sherpa.onnx.OfflineMoonshineModelConfig copy(java.lang.String preprocessor, java.lang.String encoder, java.lang.String uncachedDecoder, java.lang.String cachedDecoder, java.lang.String mergedDecoder) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(preprocessor, "preprocessor");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(encoder, "encoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(uncachedDecoder, "uncachedDecoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(cachedDecoder, "cachedDecoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(mergedDecoder, "mergedDecoder");
        return new com.k2fsa.sherpa.onnx.OfflineMoonshineModelConfig(preprocessor, encoder, uncachedDecoder, cachedDecoder, mergedDecoder);
    }

    public boolean equals(java.lang.Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof com.k2fsa.sherpa.onnx.OfflineMoonshineModelConfig)) {
            return false;
        }
        com.k2fsa.sherpa.onnx.OfflineMoonshineModelConfig offlineMoonshineModelConfig = (com.k2fsa.sherpa.onnx.OfflineMoonshineModelConfig) other;
        return kotlin.jvm.internal.Intrinsics.areEqual(this.preprocessor, offlineMoonshineModelConfig.preprocessor) && kotlin.jvm.internal.Intrinsics.areEqual(this.encoder, offlineMoonshineModelConfig.encoder) && kotlin.jvm.internal.Intrinsics.areEqual(this.uncachedDecoder, offlineMoonshineModelConfig.uncachedDecoder) && kotlin.jvm.internal.Intrinsics.areEqual(this.cachedDecoder, offlineMoonshineModelConfig.cachedDecoder) && kotlin.jvm.internal.Intrinsics.areEqual(this.mergedDecoder, offlineMoonshineModelConfig.mergedDecoder);
    }

    public int hashCode() {
        return (((((((this.preprocessor.hashCode() * 31) + this.encoder.hashCode()) * 31) + this.uncachedDecoder.hashCode()) * 31) + this.cachedDecoder.hashCode()) * 31) + this.mergedDecoder.hashCode();
    }

    public java.lang.String toString() {
        return "OfflineMoonshineModelConfig(preprocessor=" + this.preprocessor + ", encoder=" + this.encoder + ", uncachedDecoder=" + this.uncachedDecoder + ", cachedDecoder=" + this.cachedDecoder + ", mergedDecoder=" + this.mergedDecoder + ")";
    }

    public OfflineMoonshineModelConfig(java.lang.String preprocessor, java.lang.String encoder, java.lang.String uncachedDecoder, java.lang.String cachedDecoder, java.lang.String mergedDecoder) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(preprocessor, "preprocessor");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(encoder, "encoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(uncachedDecoder, "uncachedDecoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(cachedDecoder, "cachedDecoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(mergedDecoder, "mergedDecoder");
        this.preprocessor = preprocessor;
        this.encoder = encoder;
        this.uncachedDecoder = uncachedDecoder;
        this.cachedDecoder = cachedDecoder;
        this.mergedDecoder = mergedDecoder;
    }

    public /* synthetic */ OfflineMoonshineModelConfig(java.lang.String str, java.lang.String str2, java.lang.String str3, java.lang.String str4, java.lang.String str5, int i, kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
        this((i & 1) != 0 ? "" : str, (i & 2) != 0 ? "" : str2, (i & 4) != 0 ? "" : str3, (i & 8) != 0 ? "" : str4, (i & 16) != 0 ? "" : str5);
    }

    public final java.lang.String getPreprocessor() {
        return this.preprocessor;
    }

    public final void setPreprocessor(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.preprocessor = str;
    }

    public final java.lang.String getEncoder() {
        return this.encoder;
    }

    public final void setEncoder(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.encoder = str;
    }

    public final java.lang.String getUncachedDecoder() {
        return this.uncachedDecoder;
    }

    public final void setUncachedDecoder(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.uncachedDecoder = str;
    }

    public final java.lang.String getCachedDecoder() {
        return this.cachedDecoder;
    }

    public final void setCachedDecoder(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.cachedDecoder = str;
    }

    public final java.lang.String getMergedDecoder() {
        return this.mergedDecoder;
    }

    public final void setMergedDecoder(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.mergedDecoder = str;
    }
}
