package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: OfflineRecognizer.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010\u0014\n\u0002\b\u0005\n\u0002\u0010\u0015\n\u0002\b\u001a\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001BO\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\u0003\u0012\u0006\u0010\t\u001a\u00020\u0003\u0012\u0006\u0010\n\u001a\u00020\u0003\u0012\u0006\u0010\u000b\u001a\u00020\u0007\u0012\b\b\u0002\u0010\f\u001a\u00020\r¢\u0006\u0004\b\u000e\u0010\u000fJ\t\u0010\u001d\u001a\u00020\u0003HÆ\u0003J\u0014\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005HÆ\u0003¢\u0006\u0002\u0010\u0013J\t\u0010\u001f\u001a\u00020\u0007HÆ\u0003J\t\u0010 \u001a\u00020\u0003HÆ\u0003J\t\u0010!\u001a\u00020\u0003HÆ\u0003J\t\u0010\"\u001a\u00020\u0003HÆ\u0003J\t\u0010#\u001a\u00020\u0007HÆ\u0003J\t\u0010$\u001a\u00020\rHÆ\u0003Jd\u0010%\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u00032\b\b\u0002\u0010\u000b\u001a\u00020\u00072\b\b\u0002\u0010\f\u001a\u00020\rHÆ\u0001¢\u0006\u0002\u0010&J\u0014\u0010'\u001a\u00020(2\b\u0010)\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010*\u001a\u00020+HÖ\u0081\u0004J\n\u0010,\u001a\u00020\u0003HÖ\u0081\u0004R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0019\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005¢\u0006\n\n\u0002\u0010\u0014\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0006\u001a\u00020\u0007¢\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0011\u0010\b\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0011R\u0011\u0010\t\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0011R\u0011\u0010\n\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0011R\u0011\u0010\u000b\u001a\u00020\u0007¢\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0016R\u0011\u0010\f\u001a\u00020\r¢\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cÊ\u0001\f\b.\u0012\b\b/\u0012\u0004\b\u0003\u0010\u0000¨\u0006-"}, d2 = {"Lcom/k2fsa/sherpa/onnx/OfflineRecognizerResult;", "", "text", "", "tokens", "", "timestamps", "", "lang", "emotion", "event", "durations", "words", "", "<init>", "(Ljava/lang/String;[Ljava/lang/String;[FLjava/lang/String;Ljava/lang/String;Ljava/lang/String;[F[I)V", "getText", "()Ljava/lang/String;", "getTokens", "()[Ljava/lang/String;", "[Ljava/lang/String;", "getTimestamps", "()[F", "getLang", "getEmotion", "getEvent", "getDurations", "getWords", "()[I", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "copy", "(Ljava/lang/String;[Ljava/lang/String;[FLjava/lang/String;Ljava/lang/String;Ljava/lang/String;[F[I)Lcom/k2fsa/sherpa/onnx/OfflineRecognizerResult;", "equals", "", "other", "hashCode", "", "toString", "app", "Landroidx/compose/runtime/internal/StabilityInferred;", "parameters"}, k = 1, mv = {2, 4, 0}, xi = 48)
public final /* data */ class OfflineRecognizerResult {
    public static final int $stable = 8;
    private final float[] durations;
    private final java.lang.String emotion;
    private final java.lang.String event;
    private final java.lang.String lang;
    private final java.lang.String text;
    private final float[] timestamps;
    private final java.lang.String[] tokens;
    private final int[] words;

    public static /* synthetic */ com.k2fsa.sherpa.onnx.OfflineRecognizerResult copy$default(com.k2fsa.sherpa.onnx.OfflineRecognizerResult offlineRecognizerResult, java.lang.String str, java.lang.String[] strArr, float[] fArr, java.lang.String str2, java.lang.String str3, java.lang.String str4, float[] fArr2, int[] iArr, int i, java.lang.Object obj) {
        if ((i & 1) != 0) {
            str = offlineRecognizerResult.text;
        }
        if ((i & 2) != 0) {
            strArr = offlineRecognizerResult.tokens;
        }
        if ((i & 4) != 0) {
            fArr = offlineRecognizerResult.timestamps;
        }
        if ((i & 8) != 0) {
            str2 = offlineRecognizerResult.lang;
        }
        if ((i & 16) != 0) {
            str3 = offlineRecognizerResult.emotion;
        }
        if ((i & 32) != 0) {
            str4 = offlineRecognizerResult.event;
        }
        if ((i & 64) != 0) {
            fArr2 = offlineRecognizerResult.durations;
        }
        if ((i & 128) != 0) {
            iArr = offlineRecognizerResult.words;
        }
        float[] fArr3 = fArr2;
        int[] iArr2 = iArr;
        java.lang.String str5 = str3;
        java.lang.String str6 = str4;
        return offlineRecognizerResult.copy(str, strArr, fArr, str2, str5, str6, fArr3, iArr2);
    }

    /* JADX INFO: renamed from: component1, reason: from getter */
    public final java.lang.String getText() {
        return this.text;
    }

    /* JADX INFO: renamed from: component2, reason: from getter */
    public final java.lang.String[] getTokens() {
        return this.tokens;
    }

    /* JADX INFO: renamed from: component3, reason: from getter */
    public final float[] getTimestamps() {
        return this.timestamps;
    }

    /* JADX INFO: renamed from: component4, reason: from getter */
    public final java.lang.String getLang() {
        return this.lang;
    }

    /* JADX INFO: renamed from: component5, reason: from getter */
    public final java.lang.String getEmotion() {
        return this.emotion;
    }

    /* JADX INFO: renamed from: component6, reason: from getter */
    public final java.lang.String getEvent() {
        return this.event;
    }

    /* JADX INFO: renamed from: component7, reason: from getter */
    public final float[] getDurations() {
        return this.durations;
    }

    /* JADX INFO: renamed from: component8, reason: from getter */
    public final int[] getWords() {
        return this.words;
    }

    public final com.k2fsa.sherpa.onnx.OfflineRecognizerResult copy(java.lang.String text, java.lang.String[] tokens, float[] timestamps, java.lang.String lang, java.lang.String emotion, java.lang.String event, float[] durations, int[] words) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(text, "text");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(tokens, "tokens");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(timestamps, "timestamps");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(lang, "lang");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(emotion, "emotion");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(event, "event");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(durations, "durations");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(words, "words");
        return new com.k2fsa.sherpa.onnx.OfflineRecognizerResult(text, tokens, timestamps, lang, emotion, event, durations, words);
    }

    public boolean equals(java.lang.Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof com.k2fsa.sherpa.onnx.OfflineRecognizerResult)) {
            return false;
        }
        com.k2fsa.sherpa.onnx.OfflineRecognizerResult offlineRecognizerResult = (com.k2fsa.sherpa.onnx.OfflineRecognizerResult) other;
        return kotlin.jvm.internal.Intrinsics.areEqual(this.text, offlineRecognizerResult.text) && kotlin.jvm.internal.Intrinsics.areEqual(this.tokens, offlineRecognizerResult.tokens) && kotlin.jvm.internal.Intrinsics.areEqual(this.timestamps, offlineRecognizerResult.timestamps) && kotlin.jvm.internal.Intrinsics.areEqual(this.lang, offlineRecognizerResult.lang) && kotlin.jvm.internal.Intrinsics.areEqual(this.emotion, offlineRecognizerResult.emotion) && kotlin.jvm.internal.Intrinsics.areEqual(this.event, offlineRecognizerResult.event) && kotlin.jvm.internal.Intrinsics.areEqual(this.durations, offlineRecognizerResult.durations) && kotlin.jvm.internal.Intrinsics.areEqual(this.words, offlineRecognizerResult.words);
    }

    public int hashCode() {
        return (((((((((((((this.text.hashCode() * 31) + java.util.Arrays.hashCode(this.tokens)) * 31) + java.util.Arrays.hashCode(this.timestamps)) * 31) + this.lang.hashCode()) * 31) + this.emotion.hashCode()) * 31) + this.event.hashCode()) * 31) + java.util.Arrays.hashCode(this.durations)) * 31) + java.util.Arrays.hashCode(this.words);
    }

    public java.lang.String toString() {
        return "OfflineRecognizerResult(text=" + this.text + ", tokens=" + java.util.Arrays.toString(this.tokens) + ", timestamps=" + java.util.Arrays.toString(this.timestamps) + ", lang=" + this.lang + ", emotion=" + this.emotion + ", event=" + this.event + ", durations=" + java.util.Arrays.toString(this.durations) + ", words=" + java.util.Arrays.toString(this.words) + ")";
    }

    public OfflineRecognizerResult(java.lang.String text, java.lang.String[] tokens, float[] timestamps, java.lang.String lang, java.lang.String emotion, java.lang.String event, float[] durations, int[] words) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(text, "text");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(tokens, "tokens");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(timestamps, "timestamps");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(lang, "lang");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(emotion, "emotion");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(event, "event");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(durations, "durations");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(words, "words");
        this.text = text;
        this.tokens = tokens;
        this.timestamps = timestamps;
        this.lang = lang;
        this.emotion = emotion;
        this.event = event;
        this.durations = durations;
        this.words = words;
    }

    /* JADX WARN: Illegal instructions before constructor call */
    public /* synthetic */ OfflineRecognizerResult(java.lang.String str, java.lang.String[] strArr, float[] fArr, java.lang.String str2, java.lang.String str3, java.lang.String str4, float[] fArr2, int[] iArr, int i, kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
        int[] iArr2;
        if ((i & 128) == 0) {
            iArr2 = iArr;
        } else {
            iArr2 = new int[0];
        }
        this(str, strArr, fArr, str2, str3, str4, fArr2, iArr2);
    }

    public final java.lang.String getText() {
        return this.text;
    }

    public final java.lang.String[] getTokens() {
        return this.tokens;
    }

    public final float[] getTimestamps() {
        return this.timestamps;
    }

    public final java.lang.String getLang() {
        return this.lang;
    }

    public final java.lang.String getEmotion() {
        return this.emotion;
    }

    public final java.lang.String getEvent() {
        return this.event;
    }

    public final float[] getDurations() {
        return this.durations;
    }

    public final int[] getWords() {
        return this.words;
    }
}
