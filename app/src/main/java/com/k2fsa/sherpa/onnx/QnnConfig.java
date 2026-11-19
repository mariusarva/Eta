package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: QnnConfig.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0011\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B%\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003¢\u0006\u0004\b\u0006\u0010\u0007J\t\u0010\u0010\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0011\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0012\u001a\u00020\u0003HÆ\u0003J'\u0010\u0013\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0003HÆ\u0001J\u0014\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0017\u001a\u00020\u0018HÖ\u0081\u0004J\n\u0010\u0019\u001a\u00020\u0003HÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR\u001a\u0010\u0004\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\t\"\u0004\b\r\u0010\u000bR\u001a\u0010\u0005\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\t\"\u0004\b\u000f\u0010\u000bÊ\u0001\f\b\u001b\u0012\b\b\u001c\u0012\u0004\b\u0003\u0010\u0000¨\u0006\u001a"}, d2 = {"Lcom/k2fsa/sherpa/onnx/QnnConfig;", "", "backendLib", "", "contextBinary", "systemLib", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getBackendLib", "()Ljava/lang/String;", "setBackendLib", "(Ljava/lang/String;)V", "getContextBinary", "setContextBinary", "getSystemLib", "setSystemLib", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "app", "Landroidx/compose/runtime/internal/StabilityInferred;", "parameters"}, k = 1, mv = {2, 4, 0}, xi = 48)
public final /* data */ class QnnConfig {
    public static final int $stable = 8;
    private java.lang.String backendLib;
    private java.lang.String contextBinary;
    private java.lang.String systemLib;

    public QnnConfig() {
        this(null, null, null, 7, null);
    }

    public static /* synthetic */ com.k2fsa.sherpa.onnx.QnnConfig copy$default(com.k2fsa.sherpa.onnx.QnnConfig qnnConfig, java.lang.String str, java.lang.String str2, java.lang.String str3, int i, java.lang.Object obj) {
        if ((i & 1) != 0) {
            str = qnnConfig.backendLib;
        }
        if ((i & 2) != 0) {
            str2 = qnnConfig.contextBinary;
        }
        if ((i & 4) != 0) {
            str3 = qnnConfig.systemLib;
        }
        return qnnConfig.copy(str, str2, str3);
    }

    /* JADX INFO: renamed from: component1, reason: from getter */
    public final java.lang.String getBackendLib() {
        return this.backendLib;
    }

    /* JADX INFO: renamed from: component2, reason: from getter */
    public final java.lang.String getContextBinary() {
        return this.contextBinary;
    }

    /* JADX INFO: renamed from: component3, reason: from getter */
    public final java.lang.String getSystemLib() {
        return this.systemLib;
    }

    public final com.k2fsa.sherpa.onnx.QnnConfig copy(java.lang.String backendLib, java.lang.String contextBinary, java.lang.String systemLib) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(backendLib, "backendLib");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(contextBinary, "contextBinary");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(systemLib, "systemLib");
        return new com.k2fsa.sherpa.onnx.QnnConfig(backendLib, contextBinary, systemLib);
    }

    public boolean equals(java.lang.Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof com.k2fsa.sherpa.onnx.QnnConfig)) {
            return false;
        }
        com.k2fsa.sherpa.onnx.QnnConfig qnnConfig = (com.k2fsa.sherpa.onnx.QnnConfig) other;
        return kotlin.jvm.internal.Intrinsics.areEqual(this.backendLib, qnnConfig.backendLib) && kotlin.jvm.internal.Intrinsics.areEqual(this.contextBinary, qnnConfig.contextBinary) && kotlin.jvm.internal.Intrinsics.areEqual(this.systemLib, qnnConfig.systemLib);
    }

    public int hashCode() {
        return (((this.backendLib.hashCode() * 31) + this.contextBinary.hashCode()) * 31) + this.systemLib.hashCode();
    }

    public java.lang.String toString() {
        return "QnnConfig(backendLib=" + this.backendLib + ", contextBinary=" + this.contextBinary + ", systemLib=" + this.systemLib + ")";
    }

    public QnnConfig(java.lang.String backendLib, java.lang.String contextBinary, java.lang.String systemLib) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(backendLib, "backendLib");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(contextBinary, "contextBinary");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(systemLib, "systemLib");
        this.backendLib = backendLib;
        this.contextBinary = contextBinary;
        this.systemLib = systemLib;
    }

    public /* synthetic */ QnnConfig(java.lang.String str, java.lang.String str2, java.lang.String str3, int i, kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
        this((i & 1) != 0 ? "" : str, (i & 2) != 0 ? "" : str2, (i & 4) != 0 ? "" : str3);
    }

    public final java.lang.String getBackendLib() {
        return this.backendLib;
    }

    public final void setBackendLib(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.backendLib = str;
    }

    public final java.lang.String getContextBinary() {
        return this.contextBinary;
    }

    public final void setContextBinary(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.contextBinary = str;
    }

    public final java.lang.String getSystemLib() {
        return this.systemLib;
    }

    public final void setSystemLib(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.systemLib = str;
    }
}
