package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: OfflineRecognizer.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B\u001b\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\t\u0010\u0010\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0011\u001a\u00020\u0005HÆ\u0003J\u001d\u0010\u0012\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005HÆ\u0001J\u0014\u0010\u0013\u001a\u00020\u00142\b\u0010\u0015\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0016\u001a\u00020\u0005HÖ\u0081\u0004J\n\u0010\u0017\u001a\u00020\u0003HÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000fÊ\u0001\f\b\u0019\u0012\b\b\u001a\u0012\u0004\b\u0003\u0010\u0000¨\u0006\u0018"}, d2 = {"Lcom/k2fsa/sherpa/onnx/OfflineCtcFstDecoderConfig;", "", "graph", "", "maxActive", "", "<init>", "(Ljava/lang/String;I)V", "getGraph", "()Ljava/lang/String;", "setGraph", "(Ljava/lang/String;)V", "getMaxActive", "()I", "setMaxActive", "(I)V", "component1", "component2", "copy", "equals", "", "other", "hashCode", "toString", "app", "Landroidx/compose/runtime/internal/StabilityInferred;", "parameters"}, k = 1, mv = {2, 4, 0}, xi = 48)
public final /* data */ class OfflineCtcFstDecoderConfig {
    public static final int $stable = 8;
    private java.lang.String graph;
    private int maxActive;

    /* JADX WARN: Illegal instructions before constructor call */
    public OfflineCtcFstDecoderConfig() {
        java.lang.String str = null;
        this(str, 0, 3, str);
    }

    public static /* synthetic */ com.k2fsa.sherpa.onnx.OfflineCtcFstDecoderConfig copy$default(com.k2fsa.sherpa.onnx.OfflineCtcFstDecoderConfig offlineCtcFstDecoderConfig, java.lang.String str, int i, int i2, java.lang.Object obj) {
        if ((i2 & 1) != 0) {
            str = offlineCtcFstDecoderConfig.graph;
        }
        if ((i2 & 2) != 0) {
            i = offlineCtcFstDecoderConfig.maxActive;
        }
        return offlineCtcFstDecoderConfig.copy(str, i);
    }

    /* JADX INFO: renamed from: component1, reason: from getter */
    public final java.lang.String getGraph() {
        return this.graph;
    }

    /* JADX INFO: renamed from: component2, reason: from getter */
    public final int getMaxActive() {
        return this.maxActive;
    }

    public final com.k2fsa.sherpa.onnx.OfflineCtcFstDecoderConfig copy(java.lang.String graph, int maxActive) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(graph, "graph");
        return new com.k2fsa.sherpa.onnx.OfflineCtcFstDecoderConfig(graph, maxActive);
    }

    public boolean equals(java.lang.Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof com.k2fsa.sherpa.onnx.OfflineCtcFstDecoderConfig)) {
            return false;
        }
        com.k2fsa.sherpa.onnx.OfflineCtcFstDecoderConfig offlineCtcFstDecoderConfig = (com.k2fsa.sherpa.onnx.OfflineCtcFstDecoderConfig) other;
        return kotlin.jvm.internal.Intrinsics.areEqual(this.graph, offlineCtcFstDecoderConfig.graph) && this.maxActive == offlineCtcFstDecoderConfig.maxActive;
    }

    public int hashCode() {
        return (this.graph.hashCode() * 31) + java.lang.Integer.hashCode(this.maxActive);
    }

    public java.lang.String toString() {
        return "OfflineCtcFstDecoderConfig(graph=" + this.graph + ", maxActive=" + this.maxActive + ")";
    }

    public OfflineCtcFstDecoderConfig(java.lang.String graph, int maxActive) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(graph, "graph");
        this.graph = graph;
        this.maxActive = maxActive;
    }

    public /* synthetic */ OfflineCtcFstDecoderConfig(java.lang.String str, int i, int i2, kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
        this((i2 & 1) != 0 ? "" : str, (i2 & 2) != 0 ? 3000 : i);
    }

    public final java.lang.String getGraph() {
        return this.graph;
    }

    public final void setGraph(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.graph = str;
    }

    public final int getMaxActive() {
        return this.maxActive;
    }

    public final void setMaxActive(int i) {
        this.maxActive = i;
    }
}
