package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: HomophoneReplacerConfig.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0011\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B%\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003¢\u0006\u0004\b\u0006\u0010\u0007J\t\u0010\u0010\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0011\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0012\u001a\u00020\u0003HÆ\u0003J'\u0010\u0013\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0003HÆ\u0001J\u0014\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0017\u001a\u00020\u0018HÖ\u0081\u0004J\n\u0010\u0019\u001a\u00020\u0003HÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR\u001a\u0010\u0004\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\t\"\u0004\b\r\u0010\u000bR\u001a\u0010\u0005\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\t\"\u0004\b\u000f\u0010\u000bÊ\u0001\f\b\u001b\u0012\b\b\u001c\u0012\u0004\b\u0003\u0010\u0000¨\u0006\u001a"}, d2 = {"Lcom/k2fsa/sherpa/onnx/HomophoneReplacerConfig;", "", "dictDir", "", "lexicon", "ruleFsts", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getDictDir", "()Ljava/lang/String;", "setDictDir", "(Ljava/lang/String;)V", "getLexicon", "setLexicon", "getRuleFsts", "setRuleFsts", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "app", "Landroidx/compose/runtime/internal/StabilityInferred;", "parameters"}, k = 1, mv = {2, 4, 0}, xi = 48)
public final /* data */ class HomophoneReplacerConfig {
    public static final int $stable = 8;
    private java.lang.String dictDir;
    private java.lang.String lexicon;
    private java.lang.String ruleFsts;

    public HomophoneReplacerConfig() {
        this(null, null, null, 7, null);
    }

    public static /* synthetic */ com.k2fsa.sherpa.onnx.HomophoneReplacerConfig copy$default(com.k2fsa.sherpa.onnx.HomophoneReplacerConfig homophoneReplacerConfig, java.lang.String str, java.lang.String str2, java.lang.String str3, int i, java.lang.Object obj) {
        if ((i & 1) != 0) {
            str = homophoneReplacerConfig.dictDir;
        }
        if ((i & 2) != 0) {
            str2 = homophoneReplacerConfig.lexicon;
        }
        if ((i & 4) != 0) {
            str3 = homophoneReplacerConfig.ruleFsts;
        }
        return homophoneReplacerConfig.copy(str, str2, str3);
    }

    /* JADX INFO: renamed from: component1, reason: from getter */
    public final java.lang.String getDictDir() {
        return this.dictDir;
    }

    /* JADX INFO: renamed from: component2, reason: from getter */
    public final java.lang.String getLexicon() {
        return this.lexicon;
    }

    /* JADX INFO: renamed from: component3, reason: from getter */
    public final java.lang.String getRuleFsts() {
        return this.ruleFsts;
    }

    public final com.k2fsa.sherpa.onnx.HomophoneReplacerConfig copy(java.lang.String dictDir, java.lang.String lexicon, java.lang.String ruleFsts) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(dictDir, "dictDir");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(lexicon, "lexicon");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(ruleFsts, "ruleFsts");
        return new com.k2fsa.sherpa.onnx.HomophoneReplacerConfig(dictDir, lexicon, ruleFsts);
    }

    public boolean equals(java.lang.Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof com.k2fsa.sherpa.onnx.HomophoneReplacerConfig)) {
            return false;
        }
        com.k2fsa.sherpa.onnx.HomophoneReplacerConfig homophoneReplacerConfig = (com.k2fsa.sherpa.onnx.HomophoneReplacerConfig) other;
        return kotlin.jvm.internal.Intrinsics.areEqual(this.dictDir, homophoneReplacerConfig.dictDir) && kotlin.jvm.internal.Intrinsics.areEqual(this.lexicon, homophoneReplacerConfig.lexicon) && kotlin.jvm.internal.Intrinsics.areEqual(this.ruleFsts, homophoneReplacerConfig.ruleFsts);
    }

    public int hashCode() {
        return (((this.dictDir.hashCode() * 31) + this.lexicon.hashCode()) * 31) + this.ruleFsts.hashCode();
    }

    public java.lang.String toString() {
        return "HomophoneReplacerConfig(dictDir=" + this.dictDir + ", lexicon=" + this.lexicon + ", ruleFsts=" + this.ruleFsts + ")";
    }

    public HomophoneReplacerConfig(java.lang.String dictDir, java.lang.String lexicon, java.lang.String ruleFsts) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(dictDir, "dictDir");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(lexicon, "lexicon");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(ruleFsts, "ruleFsts");
        this.dictDir = dictDir;
        this.lexicon = lexicon;
        this.ruleFsts = ruleFsts;
    }

    public /* synthetic */ HomophoneReplacerConfig(java.lang.String str, java.lang.String str2, java.lang.String str3, int i, kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
        this((i & 1) != 0 ? "" : str, (i & 2) != 0 ? "" : str2, (i & 4) != 0 ? "" : str3);
    }

    public final java.lang.String getDictDir() {
        return this.dictDir;
    }

    public final void setDictDir(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.dictDir = str;
    }

    public final java.lang.String getLexicon() {
        return this.lexicon;
    }

    public final void setLexicon(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.lexicon = str;
    }

    public final java.lang.String getRuleFsts() {
        return this.ruleFsts;
    }

    public final void setRuleFsts(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.ruleFsts = str;
    }
}
