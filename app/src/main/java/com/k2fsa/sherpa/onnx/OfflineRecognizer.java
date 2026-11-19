package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: OfflineRecognizer.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u0000 \u001d2\u00020\u0001:\u0001\u001dB\u001b\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\b\u0010\f\u001a\u00020\rH\u0004J\u0006\u0010\u000e\u001a\u00020\rJ\u0006\u0010\u000f\u001a\u00020\u0010J\u000e\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012J\u000e\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0010J\u000e\u0010\u0016\u001a\u00020\r2\u0006\u0010\u0015\u001a\u00020\u0010J\u000e\u0010\u0017\u001a\u00020\r2\u0006\u0010\u0004\u001a\u00020\u0005J\u0011\u0010\u0018\u001a\u00020\r2\u0006\u0010\n\u001a\u00020\u000bH\u0082 J\u0011\u0010\u000f\u001a\u00020\u000b2\u0006\u0010\n\u001a\u00020\u000bH\u0082 J\u0019\u0010\u0019\u001a\u00020\u000b2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0011\u001a\u00020\u0012H\u0082 J\u0019\u0010\u0017\u001a\u00020\r2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0004\u001a\u00020\u0005H\u0082 J\u0019\u0010\u001a\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u0082 J\u0011\u0010\u001b\u001a\u00020\u000b2\u0006\u0010\u0004\u001a\u00020\u0005H\u0082 J\u0019\u0010\u0016\u001a\u00020\r2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u001c\u001a\u00020\u000bH\u0082 J\u0011\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u001c\u001a\u00020\u000bH\u0082 R\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u000e\u0010\n\u001a\u00020\u000bX\u0082\u000e¢\u0006\u0002\n\u0000Ê\u0001\f\b\u001f\u0012\b\b \u0012\u0004\b\u0003\u0010\u0000¨\u0006\u001e"}, d2 = {"Lcom/k2fsa/sherpa/onnx/OfflineRecognizer;", "", "assetManager", "Landroid/content/res/AssetManager;", "config", "Lcom/k2fsa/sherpa/onnx/OfflineRecognizerConfig;", "<init>", "(Landroid/content/res/AssetManager;Lcom/k2fsa/sherpa/onnx/OfflineRecognizerConfig;)V", "getConfig", "()Lcom/k2fsa/sherpa/onnx/OfflineRecognizerConfig;", "ptr", "", "finalize", "", "release", "createStream", "Lcom/k2fsa/sherpa/onnx/OfflineStream;", "hotwords", "", "getResult", "Lcom/k2fsa/sherpa/onnx/OfflineRecognizerResult;", "stream", "decode", "setConfig", "delete", "createStreamWithHotwords", "newFromAsset", "newFromFile", "streamPtr", "Companion", "app", "Landroidx/compose/runtime/internal/StabilityInferred;", "parameters"}, k = 1, mv = {2, 4, 0}, xi = 48)
public final class OfflineRecognizer {
    private final com.k2fsa.sherpa.onnx.OfflineRecognizerConfig config;
    private long ptr;

    /* JADX INFO: renamed from: Companion, reason: from kotlin metadata */
    public static final com.k2fsa.sherpa.onnx.OfflineRecognizer.Companion INSTANCE = new com.k2fsa.sherpa.onnx.OfflineRecognizer.Companion(null);
    public static final int $stable = 8;

    private final native long createStream(long ptr);

    private final native long createStreamWithHotwords(long ptr, java.lang.String hotwords);

    private final native void decode(long ptr, long streamPtr);

    private final native void delete(long ptr);

    private final native com.k2fsa.sherpa.onnx.OfflineRecognizerResult getResult(long streamPtr);

    private final native long newFromAsset(android.content.res.AssetManager assetManager, com.k2fsa.sherpa.onnx.OfflineRecognizerConfig config);

    private final native long newFromFile(com.k2fsa.sherpa.onnx.OfflineRecognizerConfig config);

    @kotlin.jvm.JvmStatic
    public static final native void prependAdspLibraryPath(java.lang.String str);

    private final native void setConfig(long ptr, com.k2fsa.sherpa.onnx.OfflineRecognizerConfig config);

    public OfflineRecognizer(android.content.res.AssetManager assetManager, com.k2fsa.sherpa.onnx.OfflineRecognizerConfig config) {
        long jNewFromFile;
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(config, "config");
        this.config = config;
        if (assetManager != null) {
            jNewFromFile = newFromAsset(assetManager, this.config);
        } else {
            jNewFromFile = newFromFile(this.config);
        }
        this.ptr = jNewFromFile;
        if (this.ptr != 0) {
        } else {
            throw new java.lang.IllegalArgumentException("Invalid OfflineRecognizerConfig: failed to create native OfflineRecognizer".toString());
        }
    }

    public /* synthetic */ OfflineRecognizer(android.content.res.AssetManager assetManager, com.k2fsa.sherpa.onnx.OfflineRecognizerConfig offlineRecognizerConfig, int i, kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
        this((i & 1) != 0 ? null : assetManager, offlineRecognizerConfig);
    }

    public final com.k2fsa.sherpa.onnx.OfflineRecognizerConfig getConfig() {
        return this.config;
    }

    protected final void finalize() {
        if (this.ptr != 0) {
            delete(this.ptr);
            this.ptr = 0L;
        }
    }

    public final void release() {
        finalize();
    }

    public final com.k2fsa.sherpa.onnx.OfflineStream createStream() {
        long p = createStream(this.ptr);
        return new com.k2fsa.sherpa.onnx.OfflineStream(p);
    }

    public final com.k2fsa.sherpa.onnx.OfflineStream createStream(java.lang.String hotwords) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(hotwords, "hotwords");
        long p = createStreamWithHotwords(this.ptr, hotwords);
        return new com.k2fsa.sherpa.onnx.OfflineStream(p);
    }

    public final com.k2fsa.sherpa.onnx.OfflineRecognizerResult getResult(com.k2fsa.sherpa.onnx.OfflineStream stream) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(stream, "stream");
        return getResult(stream.getPtr());
    }

    public final void decode(com.k2fsa.sherpa.onnx.OfflineStream stream) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(stream, "stream");
        decode(this.ptr, stream.getPtr());
    }

    public final void setConfig(com.k2fsa.sherpa.onnx.OfflineRecognizerConfig config) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(config, "config");
        setConfig(this.ptr, config);
    }

    /* JADX INFO: compiled from: OfflineRecognizer.kt */
    @kotlin.Metadata(d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u0015\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0087 b\u0002\b\b¨\u0006\t"}, d2 = {"Lcom/k2fsa/sherpa/onnx/OfflineRecognizer$Companion;", "", "<init>", "()V", "prependAdspLibraryPath", "", "newPath", "", "Lkotlin/jvm/JvmStatic;", "app"}, k = 1, mv = {2, 4, 0}, xi = 48)
    public static final class Companion {
        public /* synthetic */ Companion(kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        @kotlin.jvm.JvmStatic
        public final void prependAdspLibraryPath(java.lang.String newPath) {
            com.k2fsa.sherpa.onnx.OfflineRecognizer.prependAdspLibraryPath(newPath);
        }
    }

    static {
        java.lang.System.loadLibrary("sherpa-onnx-jni");
    }
}
