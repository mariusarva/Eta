package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: OfflineRecognizer.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b$\n\u0002\u0018\u0002\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001BM\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u0012\b\b\u0002\u0010\u000b\u001a\u00020\n¢\u0006\u0004\b\f\u0010\rJ\t\u0010\"\u001a\u00020\u0003HÆ\u0003J\t\u0010#\u001a\u00020\u0003HÆ\u0003J\t\u0010$\u001a\u00020\u0003HÆ\u0003J\t\u0010%\u001a\u00020\u0003HÆ\u0003J\t\u0010&\u001a\u00020\bHÆ\u0003J\t\u0010'\u001a\u00020\nHÆ\u0003J\t\u0010(\u001a\u00020\nHÆ\u0003JO\u0010)\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\nHÆ\u0001J\u0014\u0010*\u001a\u00020\n2\b\u0010+\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010,\u001a\u00020\bHÖ\u0081\u0004J\n\u0010-\u001a\u00020\u0003HÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011R\u001a\u0010\u0004\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u000f\"\u0004\b\u0013\u0010\u0011R\u001a\u0010\u0005\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0014\u0010\u000f\"\u0004\b\u0015\u0010\u0011R\u001a\u0010\u0006\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\u000f\"\u0004\b\u0017\u0010\u0011R\u001a\u0010\u0007\u001a\u00020\bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0018\u0010\u0019\"\u0004\b\u001a\u0010\u001bR\u001a\u0010\t\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001c\u0010\u001d\"\u0004\b\u001e\u0010\u001fR\u001a\u0010\u000b\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b \u0010\u001d\"\u0004\b!\u0010\u001fÊ\u0001\f\b/\u0012\b\b0\u0012\u0004\b\u0003\u0010\u0000¨\u0006."}, d2 = {"Lcom/k2fsa/sherpa/onnx/OfflineWhisperModelConfig;", "", "encoder", "", "decoder", "language", "task", "tailPaddings", "", "enableTokenTimestamps", "", "enableSegmentTimestamps", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IZZ)V", "getEncoder", "()Ljava/lang/String;", "setEncoder", "(Ljava/lang/String;)V", "getDecoder", "setDecoder", "getLanguage", "setLanguage", "getTask", "setTask", "getTailPaddings", "()I", "setTailPaddings", "(I)V", "getEnableTokenTimestamps", "()Z", "setEnableTokenTimestamps", "(Z)V", "getEnableSegmentTimestamps", "setEnableSegmentTimestamps", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "other", "hashCode", "toString", "app", "Landroidx/compose/runtime/internal/StabilityInferred;", "parameters"}, k = 1, mv = {2, 4, 0}, xi = 48)
public final /* data */ class OfflineWhisperModelConfig {
    public static final int $stable = 8;
    private java.lang.String decoder;
    private boolean enableSegmentTimestamps;
    private boolean enableTokenTimestamps;
    private java.lang.String encoder;
    private java.lang.String language;
    private int tailPaddings;
    private java.lang.String task;

    public OfflineWhisperModelConfig() {
        this(null, null, null, null, 0, false, false, 127, null);
    }

    public static /* synthetic */ com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig copy$default(com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig offlineWhisperModelConfig, java.lang.String str, java.lang.String str2, java.lang.String str3, java.lang.String str4, int i, boolean z, boolean z2, int i2, java.lang.Object obj) {
        if ((i2 & 1) != 0) {
            str = offlineWhisperModelConfig.encoder;
        }
        if ((i2 & 2) != 0) {
            str2 = offlineWhisperModelConfig.decoder;
        }
        if ((i2 & 4) != 0) {
            str3 = offlineWhisperModelConfig.language;
        }
        if ((i2 & 8) != 0) {
            str4 = offlineWhisperModelConfig.task;
        }
        if ((i2 & 16) != 0) {
            i = offlineWhisperModelConfig.tailPaddings;
        }
        if ((i2 & 32) != 0) {
            z = offlineWhisperModelConfig.enableTokenTimestamps;
        }
        if ((i2 & 64) != 0) {
            z2 = offlineWhisperModelConfig.enableSegmentTimestamps;
        }
        boolean z3 = z;
        boolean z4 = z2;
        int i3 = i;
        java.lang.String str5 = str3;
        return offlineWhisperModelConfig.copy(str, str2, str5, str4, i3, z3, z4);
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
    public final java.lang.String getTask() {
        return this.task;
    }

    /* JADX INFO: renamed from: component5, reason: from getter */
    public final int getTailPaddings() {
        return this.tailPaddings;
    }

    /* JADX INFO: renamed from: component6, reason: from getter */
    public final boolean getEnableTokenTimestamps() {
        return this.enableTokenTimestamps;
    }

    /* JADX INFO: renamed from: component7, reason: from getter */
    public final boolean getEnableSegmentTimestamps() {
        return this.enableSegmentTimestamps;
    }

    public final com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig copy(java.lang.String encoder, java.lang.String decoder, java.lang.String language, java.lang.String task, int tailPaddings, boolean enableTokenTimestamps, boolean enableSegmentTimestamps) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(encoder, "encoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(decoder, "decoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(language, "language");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(task, "task");
        return new com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig(encoder, decoder, language, task, tailPaddings, enableTokenTimestamps, enableSegmentTimestamps);
    }

    public boolean equals(java.lang.Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig)) {
            return false;
        }
        com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig offlineWhisperModelConfig = (com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig) other;
        return kotlin.jvm.internal.Intrinsics.areEqual(this.encoder, offlineWhisperModelConfig.encoder) && kotlin.jvm.internal.Intrinsics.areEqual(this.decoder, offlineWhisperModelConfig.decoder) && kotlin.jvm.internal.Intrinsics.areEqual(this.language, offlineWhisperModelConfig.language) && kotlin.jvm.internal.Intrinsics.areEqual(this.task, offlineWhisperModelConfig.task) && this.tailPaddings == offlineWhisperModelConfig.tailPaddings && this.enableTokenTimestamps == offlineWhisperModelConfig.enableTokenTimestamps && this.enableSegmentTimestamps == offlineWhisperModelConfig.enableSegmentTimestamps;
    }

    public int hashCode() {
        return (((((((((((this.encoder.hashCode() * 31) + this.decoder.hashCode()) * 31) + this.language.hashCode()) * 31) + this.task.hashCode()) * 31) + java.lang.Integer.hashCode(this.tailPaddings)) * 31) + java.lang.Boolean.hashCode(this.enableTokenTimestamps)) * 31) + java.lang.Boolean.hashCode(this.enableSegmentTimestamps);
    }

    public java.lang.String toString() {
        return "OfflineWhisperModelConfig(encoder=" + this.encoder + ", decoder=" + this.decoder + ", language=" + this.language + ", task=" + this.task + ", tailPaddings=" + this.tailPaddings + ", enableTokenTimestamps=" + this.enableTokenTimestamps + ", enableSegmentTimestamps=" + this.enableSegmentTimestamps + ")";
    }

    public OfflineWhisperModelConfig(java.lang.String encoder, java.lang.String decoder, java.lang.String language, java.lang.String task, int tailPaddings, boolean enableTokenTimestamps, boolean enableSegmentTimestamps) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(encoder, "encoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(decoder, "decoder");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(language, "language");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(task, "task");
        this.encoder = encoder;
        this.decoder = decoder;
        this.language = language;
        this.task = task;
        this.tailPaddings = tailPaddings;
        this.enableTokenTimestamps = enableTokenTimestamps;
        this.enableSegmentTimestamps = enableSegmentTimestamps;
    }

    public /* synthetic */ OfflineWhisperModelConfig(java.lang.String str, java.lang.String str2, java.lang.String str3, java.lang.String str4, int i, boolean z, boolean z2, int i2, kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
        this((i2 & 1) != 0 ? "" : str, (i2 & 2) != 0 ? "" : str2, (i2 & 4) != 0 ? "en" : str3, (i2 & 8) != 0 ? "transcribe" : str4, (i2 & 16) != 0 ? 1000 : i, (i2 & 32) != 0 ? false : z, (i2 & 64) != 0 ? false : z2);
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

    public final java.lang.String getTask() {
        return this.task;
    }

    public final void setTask(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.task = str;
    }

    public final int getTailPaddings() {
        return this.tailPaddings;
    }

    public final void setTailPaddings(int i) {
        this.tailPaddings = i;
    }

    public final boolean getEnableTokenTimestamps() {
        return this.enableTokenTimestamps;
    }

    public final void setEnableTokenTimestamps(boolean z) {
        this.enableTokenTimestamps = z;
    }

    public final boolean getEnableSegmentTimestamps() {
        return this.enableSegmentTimestamps;
    }

    public final void setEnableSegmentTimestamps(boolean z) {
        this.enableSegmentTimestamps = z;
    }
}
