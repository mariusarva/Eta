package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: OfflineStream.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u0014\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u0000 \u001a2\u00020\u0001:\u0001\u001aB\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0004\b\u0004\u0010\u0005J\u0016\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eJ\u0016\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0011J\u000e\u0010\u0013\u001a\u00020\u00112\u0006\u0010\u0010\u001a\u00020\u0011J\b\u0010\u0014\u001a\u00020\nH\u0004J\u0006\u0010\u0015\u001a\u00020\nJ\u001a\u0010\u0016\u001a\u00020\n2\u0012\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\u0000\u0012\u0004\u0012\u00020\n0\u0018J!\u0010\t\u001a\u00020\n2\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0082 J!\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0011H\u0082 J\u0019\u0010\u0013\u001a\u00020\u00112\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0010\u001a\u00020\u0011H\u0082 J\u0011\u0010\u0019\u001a\u00020\n2\u0006\u0010\u0002\u001a\u00020\u0003H\u0082 R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\u0005Ê\u0001\f\b\u001c\u0012\b\b\u001d\u0012\u0004\b\u0003\u0010\u0000¨\u0006\u001b"}, d2 = {"Lcom/k2fsa/sherpa/onnx/OfflineStream;", "", "ptr", "", "<init>", "(J)V", "getPtr", "()J", "setPtr", "acceptWaveform", "", "samples", "", "sampleRate", "", "setOption", "key", "", "value", "getOption", "finalize", "release", "use", "block", "Lkotlin/Function1;", "delete", "Companion", "app", "Landroidx/compose/runtime/internal/StabilityInferred;", "parameters"}, k = 1, mv = {2, 4, 0}, xi = 48)
public final class OfflineStream {
    private long ptr;
    public static final int $stable = 8;

    private final native void acceptWaveform(long ptr, float[] samples, int sampleRate);

    private final native void delete(long ptr);

    private final native java.lang.String getOption(long ptr, java.lang.String key);

    private final native void setOption(long ptr, java.lang.String key, java.lang.String value);

    public OfflineStream(long ptr) {
        this.ptr = ptr;
        if (!(this.ptr != 0)) {
            throw new java.lang.IllegalArgumentException("Failed to create native OfflineStream".toString());
        }
    }

    public final long getPtr() {
        return this.ptr;
    }

    public final void setPtr(long j) {
        this.ptr = j;
    }

    public final void acceptWaveform(float[] samples, int sampleRate) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(samples, "samples");
        acceptWaveform(this.ptr, samples, sampleRate);
    }

    public final void setOption(java.lang.String key, java.lang.String value) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(key, "key");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(value, "value");
        setOption(this.ptr, key, value);
    }

    public final java.lang.String getOption(java.lang.String key) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(key, "key");
        return getOption(this.ptr, key);
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

    public final void use(kotlin.jvm.functions.Function1<? super com.k2fsa.sherpa.onnx.OfflineStream, kotlin.Unit> block) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(block, "block");
        try {
            block.invoke(this);
        } finally {
            release();
        }
    }

    static {
        java.lang.System.loadLibrary("sherpa-onnx-jni");
    }
}
