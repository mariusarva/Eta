package com.k2fsa.sherpa.onnx;

/* JADX INFO: compiled from: OfflineRecognizer.kt */
/* JADX INFO: loaded from: /workspace/临时/refapk/dex/classes10.dex */
@kotlin.Metadata(d1 = {"\u0000\u008b\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0003\b\u0080\u0001\n\u0002\u0018\u0002\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B\u0081\u0002\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\r\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u000f\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0011\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u0013\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0015\u0012\b\b\u0002\u0010\u0016\u001a\u00020\u0017\u0012\b\b\u0002\u0010\u0018\u001a\u00020\u0019\u0012\b\b\u0002\u0010\u001a\u001a\u00020\u001b\u0012\b\b\u0002\u0010\u001c\u001a\u00020\u001d\u0012\b\b\u0002\u0010\u001e\u001a\u00020\u001f\u0012\b\b\u0002\u0010 \u001a\u00020!\u0012\b\b\u0002\u0010\"\u001a\u00020#\u0012\b\b\u0002\u0010$\u001a\u00020%\u0012\b\b\u0002\u0010&\u001a\u00020'\u0012\b\b\u0002\u0010(\u001a\u00020)\u0012\b\b\u0002\u0010*\u001a\u00020%\u0012\b\b\u0002\u0010+\u001a\u00020%\u0012\b\b\u0002\u0010,\u001a\u00020%\u0012\b\b\u0002\u0010-\u001a\u00020%\u0012\b\b\u0002\u0010.\u001a\u00020%¢\u0006\u0004\b/\u00100J\n\u0010\u008b\u0001\u001a\u00020\u0003HÆ\u0003J\n\u0010\u008c\u0001\u001a\u00020\u0005HÆ\u0003J\n\u0010\u008d\u0001\u001a\u00020\u0007HÆ\u0003J\n\u0010\u008e\u0001\u001a\u00020\tHÆ\u0003J\n\u0010\u008f\u0001\u001a\u00020\u000bHÆ\u0003J\n\u0010\u0090\u0001\u001a\u00020\rHÆ\u0003J\n\u0010\u0091\u0001\u001a\u00020\u000fHÆ\u0003J\n\u0010\u0092\u0001\u001a\u00020\u0011HÆ\u0003J\n\u0010\u0093\u0001\u001a\u00020\u0013HÆ\u0003J\n\u0010\u0094\u0001\u001a\u00020\u0015HÆ\u0003J\n\u0010\u0095\u0001\u001a\u00020\u0017HÆ\u0003J\n\u0010\u0096\u0001\u001a\u00020\u0019HÆ\u0003J\n\u0010\u0097\u0001\u001a\u00020\u001bHÆ\u0003J\n\u0010\u0098\u0001\u001a\u00020\u001dHÆ\u0003J\n\u0010\u0099\u0001\u001a\u00020\u001fHÆ\u0003J\n\u0010\u009a\u0001\u001a\u00020!HÆ\u0003J\n\u0010\u009b\u0001\u001a\u00020#HÆ\u0003J\n\u0010\u009c\u0001\u001a\u00020%HÆ\u0003J\n\u0010\u009d\u0001\u001a\u00020'HÆ\u0003J\n\u0010\u009e\u0001\u001a\u00020)HÆ\u0003J\n\u0010\u009f\u0001\u001a\u00020%HÆ\u0003J\n\u0010 \u0001\u001a\u00020%HÆ\u0003J\n\u0010¡\u0001\u001a\u00020%HÆ\u0003J\n\u0010¢\u0001\u001a\u00020%HÆ\u0003J\n\u0010£\u0001\u001a\u00020%HÆ\u0003J\u0084\u0002\u0010¤\u0001\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u00112\b\b\u0002\u0010\u0012\u001a\u00020\u00132\b\b\u0002\u0010\u0014\u001a\u00020\u00152\b\b\u0002\u0010\u0016\u001a\u00020\u00172\b\b\u0002\u0010\u0018\u001a\u00020\u00192\b\b\u0002\u0010\u001a\u001a\u00020\u001b2\b\b\u0002\u0010\u001c\u001a\u00020\u001d2\b\b\u0002\u0010\u001e\u001a\u00020\u001f2\b\b\u0002\u0010 \u001a\u00020!2\b\b\u0002\u0010\"\u001a\u00020#2\b\b\u0002\u0010$\u001a\u00020%2\b\b\u0002\u0010&\u001a\u00020'2\b\b\u0002\u0010(\u001a\u00020)2\b\b\u0002\u0010*\u001a\u00020%2\b\b\u0002\u0010+\u001a\u00020%2\b\b\u0002\u0010,\u001a\u00020%2\b\b\u0002\u0010-\u001a\u00020%2\b\b\u0002\u0010.\u001a\u00020%HÆ\u0001J\u0016\u0010¥\u0001\u001a\u00020)2\t\u0010¦\u0001\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\u000b\u0010§\u0001\u001a\u00020'HÖ\u0081\u0004J\u000b\u0010¨\u0001\u001a\u00020%HÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b1\u00102\"\u0004\b3\u00104R\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b5\u00106\"\u0004\b7\u00108R\u001a\u0010\u0006\u001a\u00020\u0007X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b9\u0010:\"\u0004\b;\u0010<R\u001a\u0010\b\u001a\u00020\tX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b=\u0010>\"\u0004\b?\u0010@R\u001a\u0010\n\u001a\u00020\u000bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bA\u0010B\"\u0004\bC\u0010DR\u001a\u0010\f\u001a\u00020\rX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bE\u0010F\"\u0004\bG\u0010HR\u001a\u0010\u000e\u001a\u00020\u000fX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bI\u0010J\"\u0004\bK\u0010LR\u001a\u0010\u0010\u001a\u00020\u0011X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bM\u0010N\"\u0004\bO\u0010PR\u001a\u0010\u0012\u001a\u00020\u0013X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bQ\u0010R\"\u0004\bS\u0010TR\u001a\u0010\u0014\u001a\u00020\u0015X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bU\u0010V\"\u0004\bW\u0010XR\u001a\u0010\u0016\u001a\u00020\u0017X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bY\u0010Z\"\u0004\b[\u0010\\R\u001a\u0010\u0018\u001a\u00020\u0019X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b]\u0010^\"\u0004\b_\u0010`R\u001a\u0010\u001a\u001a\u00020\u001bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\ba\u0010b\"\u0004\bc\u0010dR\u001a\u0010\u001c\u001a\u00020\u001dX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\be\u0010f\"\u0004\bg\u0010hR\u001a\u0010\u001e\u001a\u00020\u001fX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bi\u0010j\"\u0004\bk\u0010lR\u001a\u0010 \u001a\u00020!X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bm\u0010n\"\u0004\bo\u0010pR\u001a\u0010\"\u001a\u00020#X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bq\u0010r\"\u0004\bs\u0010tR\u001a\u0010$\u001a\u00020%X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bu\u0010v\"\u0004\bw\u0010xR\u001a\u0010&\u001a\u00020'X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\by\u0010z\"\u0004\b{\u0010|R\u001b\u0010(\u001a\u00020)X\u0086\u000e¢\u0006\u000f\n\u0000\u001a\u0004\b}\u0010~\"\u0005\b\u007f\u0010\u0080\u0001R\u001c\u0010*\u001a\u00020%X\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\b\u0081\u0001\u0010v\"\u0005\b\u0082\u0001\u0010xR\u001c\u0010+\u001a\u00020%X\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\b\u0083\u0001\u0010v\"\u0005\b\u0084\u0001\u0010xR\u001c\u0010,\u001a\u00020%X\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\b\u0085\u0001\u0010v\"\u0005\b\u0086\u0001\u0010xR\u001c\u0010-\u001a\u00020%X\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\b\u0087\u0001\u0010v\"\u0005\b\u0088\u0001\u0010xR\u001c\u0010.\u001a\u00020%X\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\b\u0089\u0001\u0010v\"\u0005\b\u008a\u0001\u0010xÊ\u0001\u000e\bª\u0001\u0012\t\b«\u0001\u0012\u0004\b\u0003\u0010\u0000¨\u0006©\u0001"}, d2 = {"Lcom/k2fsa/sherpa/onnx/OfflineModelConfig;", "", "transducer", "Lcom/k2fsa/sherpa/onnx/OfflineTransducerModelConfig;", "paraformer", "Lcom/k2fsa/sherpa/onnx/OfflineParaformerModelConfig;", "whisper", "Lcom/k2fsa/sherpa/onnx/OfflineWhisperModelConfig;", "fireRedAsr", "Lcom/k2fsa/sherpa/onnx/OfflineFireRedAsrModelConfig;", "moonshine", "Lcom/k2fsa/sherpa/onnx/OfflineMoonshineModelConfig;", "nemo", "Lcom/k2fsa/sherpa/onnx/OfflineNemoEncDecCtcModelConfig;", "senseVoice", "Lcom/k2fsa/sherpa/onnx/OfflineSenseVoiceModelConfig;", "dolphin", "Lcom/k2fsa/sherpa/onnx/OfflineDolphinModelConfig;", "zipformerCtc", "Lcom/k2fsa/sherpa/onnx/OfflineZipformerCtcModelConfig;", "wenetCtc", "Lcom/k2fsa/sherpa/onnx/OfflineWenetCtcModelConfig;", "omnilingual", "Lcom/k2fsa/sherpa/onnx/OfflineOmnilingualAsrCtcModelConfig;", "medasr", "Lcom/k2fsa/sherpa/onnx/OfflineMedAsrCtcModelConfig;", "funasrNano", "Lcom/k2fsa/sherpa/onnx/OfflineFunAsrNanoModelConfig;", "qwen3Asr", "Lcom/k2fsa/sherpa/onnx/OfflineQwen3AsrModelConfig;", "fireRedAsrCtc", "Lcom/k2fsa/sherpa/onnx/OfflineFireRedAsrCtcModelConfig;", "canary", "Lcom/k2fsa/sherpa/onnx/OfflineCanaryModelConfig;", "cohereTranscribe", "Lcom/k2fsa/sherpa/onnx/OfflineCohereTranscribeModelConfig;", "teleSpeech", "", "numThreads", "", "debug", "", "provider", "modelType", "tokens", "modelingUnit", "bpeVocab", "<init>", "(Lcom/k2fsa/sherpa/onnx/OfflineTransducerModelConfig;Lcom/k2fsa/sherpa/onnx/OfflineParaformerModelConfig;Lcom/k2fsa/sherpa/onnx/OfflineWhisperModelConfig;Lcom/k2fsa/sherpa/onnx/OfflineFireRedAsrModelConfig;Lcom/k2fsa/sherpa/onnx/OfflineMoonshineModelConfig;Lcom/k2fsa/sherpa/onnx/OfflineNemoEncDecCtcModelConfig;Lcom/k2fsa/sherpa/onnx/OfflineSenseVoiceModelConfig;Lcom/k2fsa/sherpa/onnx/OfflineDolphinModelConfig;Lcom/k2fsa/sherpa/onnx/OfflineZipformerCtcModelConfig;Lcom/k2fsa/sherpa/onnx/OfflineWenetCtcModelConfig;Lcom/k2fsa/sherpa/onnx/OfflineOmnilingualAsrCtcModelConfig;Lcom/k2fsa/sherpa/onnx/OfflineMedAsrCtcModelConfig;Lcom/k2fsa/sherpa/onnx/OfflineFunAsrNanoModelConfig;Lcom/k2fsa/sherpa/onnx/OfflineQwen3AsrModelConfig;Lcom/k2fsa/sherpa/onnx/OfflineFireRedAsrCtcModelConfig;Lcom/k2fsa/sherpa/onnx/OfflineCanaryModelConfig;Lcom/k2fsa/sherpa/onnx/OfflineCohereTranscribeModelConfig;Ljava/lang/String;IZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getTransducer", "()Lcom/k2fsa/sherpa/onnx/OfflineTransducerModelConfig;", "setTransducer", "(Lcom/k2fsa/sherpa/onnx/OfflineTransducerModelConfig;)V", "getParaformer", "()Lcom/k2fsa/sherpa/onnx/OfflineParaformerModelConfig;", "setParaformer", "(Lcom/k2fsa/sherpa/onnx/OfflineParaformerModelConfig;)V", "getWhisper", "()Lcom/k2fsa/sherpa/onnx/OfflineWhisperModelConfig;", "setWhisper", "(Lcom/k2fsa/sherpa/onnx/OfflineWhisperModelConfig;)V", "getFireRedAsr", "()Lcom/k2fsa/sherpa/onnx/OfflineFireRedAsrModelConfig;", "setFireRedAsr", "(Lcom/k2fsa/sherpa/onnx/OfflineFireRedAsrModelConfig;)V", "getMoonshine", "()Lcom/k2fsa/sherpa/onnx/OfflineMoonshineModelConfig;", "setMoonshine", "(Lcom/k2fsa/sherpa/onnx/OfflineMoonshineModelConfig;)V", "getNemo", "()Lcom/k2fsa/sherpa/onnx/OfflineNemoEncDecCtcModelConfig;", "setNemo", "(Lcom/k2fsa/sherpa/onnx/OfflineNemoEncDecCtcModelConfig;)V", "getSenseVoice", "()Lcom/k2fsa/sherpa/onnx/OfflineSenseVoiceModelConfig;", "setSenseVoice", "(Lcom/k2fsa/sherpa/onnx/OfflineSenseVoiceModelConfig;)V", "getDolphin", "()Lcom/k2fsa/sherpa/onnx/OfflineDolphinModelConfig;", "setDolphin", "(Lcom/k2fsa/sherpa/onnx/OfflineDolphinModelConfig;)V", "getZipformerCtc", "()Lcom/k2fsa/sherpa/onnx/OfflineZipformerCtcModelConfig;", "setZipformerCtc", "(Lcom/k2fsa/sherpa/onnx/OfflineZipformerCtcModelConfig;)V", "getWenetCtc", "()Lcom/k2fsa/sherpa/onnx/OfflineWenetCtcModelConfig;", "setWenetCtc", "(Lcom/k2fsa/sherpa/onnx/OfflineWenetCtcModelConfig;)V", "getOmnilingual", "()Lcom/k2fsa/sherpa/onnx/OfflineOmnilingualAsrCtcModelConfig;", "setOmnilingual", "(Lcom/k2fsa/sherpa/onnx/OfflineOmnilingualAsrCtcModelConfig;)V", "getMedasr", "()Lcom/k2fsa/sherpa/onnx/OfflineMedAsrCtcModelConfig;", "setMedasr", "(Lcom/k2fsa/sherpa/onnx/OfflineMedAsrCtcModelConfig;)V", "getFunasrNano", "()Lcom/k2fsa/sherpa/onnx/OfflineFunAsrNanoModelConfig;", "setFunasrNano", "(Lcom/k2fsa/sherpa/onnx/OfflineFunAsrNanoModelConfig;)V", "getQwen3Asr", "()Lcom/k2fsa/sherpa/onnx/OfflineQwen3AsrModelConfig;", "setQwen3Asr", "(Lcom/k2fsa/sherpa/onnx/OfflineQwen3AsrModelConfig;)V", "getFireRedAsrCtc", "()Lcom/k2fsa/sherpa/onnx/OfflineFireRedAsrCtcModelConfig;", "setFireRedAsrCtc", "(Lcom/k2fsa/sherpa/onnx/OfflineFireRedAsrCtcModelConfig;)V", "getCanary", "()Lcom/k2fsa/sherpa/onnx/OfflineCanaryModelConfig;", "setCanary", "(Lcom/k2fsa/sherpa/onnx/OfflineCanaryModelConfig;)V", "getCohereTranscribe", "()Lcom/k2fsa/sherpa/onnx/OfflineCohereTranscribeModelConfig;", "setCohereTranscribe", "(Lcom/k2fsa/sherpa/onnx/OfflineCohereTranscribeModelConfig;)V", "getTeleSpeech", "()Ljava/lang/String;", "setTeleSpeech", "(Ljava/lang/String;)V", "getNumThreads", "()I", "setNumThreads", "(I)V", "getDebug", "()Z", "setDebug", "(Z)V", "getProvider", "setProvider", "getModelType", "setModelType", "getTokens", "setTokens", "getModelingUnit", "setModelingUnit", "getBpeVocab", "setBpeVocab", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component20", "component21", "component22", "component23", "component24", "component25", "copy", "equals", "other", "hashCode", "toString", "app", "Landroidx/compose/runtime/internal/StabilityInferred;", "parameters"}, k = 1, mv = {2, 4, 0}, xi = 48)
public final /* data */ class OfflineModelConfig {
    public static final int $stable = 8;
    private java.lang.String bpeVocab;
    private com.k2fsa.sherpa.onnx.OfflineCanaryModelConfig canary;
    private com.k2fsa.sherpa.onnx.OfflineCohereTranscribeModelConfig cohereTranscribe;
    private boolean debug;
    private com.k2fsa.sherpa.onnx.OfflineDolphinModelConfig dolphin;
    private com.k2fsa.sherpa.onnx.OfflineFireRedAsrModelConfig fireRedAsr;
    private com.k2fsa.sherpa.onnx.OfflineFireRedAsrCtcModelConfig fireRedAsrCtc;
    private com.k2fsa.sherpa.onnx.OfflineFunAsrNanoModelConfig funasrNano;
    private com.k2fsa.sherpa.onnx.OfflineMedAsrCtcModelConfig medasr;
    private java.lang.String modelType;
    private java.lang.String modelingUnit;
    private com.k2fsa.sherpa.onnx.OfflineMoonshineModelConfig moonshine;
    private com.k2fsa.sherpa.onnx.OfflineNemoEncDecCtcModelConfig nemo;
    private int numThreads;
    private com.k2fsa.sherpa.onnx.OfflineOmnilingualAsrCtcModelConfig omnilingual;
    private com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig paraformer;
    private java.lang.String provider;
    private com.k2fsa.sherpa.onnx.OfflineQwen3AsrModelConfig qwen3Asr;
    private com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig senseVoice;
    private java.lang.String teleSpeech;
    private java.lang.String tokens;
    private com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig transducer;
    private com.k2fsa.sherpa.onnx.OfflineWenetCtcModelConfig wenetCtc;
    private com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig whisper;
    private com.k2fsa.sherpa.onnx.OfflineZipformerCtcModelConfig zipformerCtc;

    public OfflineModelConfig() {
        this(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 0, false, null, null, null, null, null, 33554431, null);
    }

    public static /* synthetic */ com.k2fsa.sherpa.onnx.OfflineModelConfig copy$default(com.k2fsa.sherpa.onnx.OfflineModelConfig offlineModelConfig, com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig offlineTransducerModelConfig, com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig offlineParaformerModelConfig, com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig offlineWhisperModelConfig, com.k2fsa.sherpa.onnx.OfflineFireRedAsrModelConfig offlineFireRedAsrModelConfig, com.k2fsa.sherpa.onnx.OfflineMoonshineModelConfig offlineMoonshineModelConfig, com.k2fsa.sherpa.onnx.OfflineNemoEncDecCtcModelConfig offlineNemoEncDecCtcModelConfig, com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig offlineSenseVoiceModelConfig, com.k2fsa.sherpa.onnx.OfflineDolphinModelConfig offlineDolphinModelConfig, com.k2fsa.sherpa.onnx.OfflineZipformerCtcModelConfig offlineZipformerCtcModelConfig, com.k2fsa.sherpa.onnx.OfflineWenetCtcModelConfig offlineWenetCtcModelConfig, com.k2fsa.sherpa.onnx.OfflineOmnilingualAsrCtcModelConfig offlineOmnilingualAsrCtcModelConfig, com.k2fsa.sherpa.onnx.OfflineMedAsrCtcModelConfig offlineMedAsrCtcModelConfig, com.k2fsa.sherpa.onnx.OfflineFunAsrNanoModelConfig offlineFunAsrNanoModelConfig, com.k2fsa.sherpa.onnx.OfflineQwen3AsrModelConfig offlineQwen3AsrModelConfig, com.k2fsa.sherpa.onnx.OfflineFireRedAsrCtcModelConfig offlineFireRedAsrCtcModelConfig, com.k2fsa.sherpa.onnx.OfflineCanaryModelConfig offlineCanaryModelConfig, com.k2fsa.sherpa.onnx.OfflineCohereTranscribeModelConfig offlineCohereTranscribeModelConfig, java.lang.String str, int i, boolean z, java.lang.String str2, java.lang.String str3, java.lang.String str4, java.lang.String str5, java.lang.String str6, int i2, java.lang.Object obj) {
        java.lang.String str7;
        java.lang.String str8;
        com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig offlineTransducerModelConfig2 = (i2 & 1) != 0 ? offlineModelConfig.transducer : offlineTransducerModelConfig;
        com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig offlineParaformerModelConfig2 = (i2 & 2) != 0 ? offlineModelConfig.paraformer : offlineParaformerModelConfig;
        com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig offlineWhisperModelConfig2 = (i2 & 4) != 0 ? offlineModelConfig.whisper : offlineWhisperModelConfig;
        com.k2fsa.sherpa.onnx.OfflineFireRedAsrModelConfig offlineFireRedAsrModelConfig2 = (i2 & 8) != 0 ? offlineModelConfig.fireRedAsr : offlineFireRedAsrModelConfig;
        com.k2fsa.sherpa.onnx.OfflineMoonshineModelConfig offlineMoonshineModelConfig2 = (i2 & 16) != 0 ? offlineModelConfig.moonshine : offlineMoonshineModelConfig;
        com.k2fsa.sherpa.onnx.OfflineNemoEncDecCtcModelConfig offlineNemoEncDecCtcModelConfig2 = (i2 & 32) != 0 ? offlineModelConfig.nemo : offlineNemoEncDecCtcModelConfig;
        com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig offlineSenseVoiceModelConfig2 = (i2 & 64) != 0 ? offlineModelConfig.senseVoice : offlineSenseVoiceModelConfig;
        com.k2fsa.sherpa.onnx.OfflineDolphinModelConfig offlineDolphinModelConfig2 = (i2 & 128) != 0 ? offlineModelConfig.dolphin : offlineDolphinModelConfig;
        com.k2fsa.sherpa.onnx.OfflineZipformerCtcModelConfig offlineZipformerCtcModelConfig2 = (i2 & io.github.mangi.eta.agent.voice.local.LocalSpeechVoskArchive.MAX_ENTRIES) != 0 ? offlineModelConfig.zipformerCtc : offlineZipformerCtcModelConfig;
        com.k2fsa.sherpa.onnx.OfflineWenetCtcModelConfig offlineWenetCtcModelConfig2 = (i2 & io.github.mangi.eta.agent.web.WebPageContent.MAX_TITLE_CHARS) != 0 ? offlineModelConfig.wenetCtc : offlineWenetCtcModelConfig;
        com.k2fsa.sherpa.onnx.OfflineOmnilingualAsrCtcModelConfig offlineOmnilingualAsrCtcModelConfig2 = (i2 & 1024) != 0 ? offlineModelConfig.omnilingual : offlineOmnilingualAsrCtcModelConfig;
        com.k2fsa.sherpa.onnx.OfflineMedAsrCtcModelConfig offlineMedAsrCtcModelConfig2 = (i2 & 2048) != 0 ? offlineModelConfig.medasr : offlineMedAsrCtcModelConfig;
        com.k2fsa.sherpa.onnx.OfflineFunAsrNanoModelConfig offlineFunAsrNanoModelConfig2 = (i2 & 4096) != 0 ? offlineModelConfig.funasrNano : offlineFunAsrNanoModelConfig;
        com.k2fsa.sherpa.onnx.OfflineQwen3AsrModelConfig offlineQwen3AsrModelConfig2 = (i2 & io.github.mangi.eta.agent.web.PublicWebSearch.MAX_URL_CHARS) != 0 ? offlineModelConfig.qwen3Asr : offlineQwen3AsrModelConfig;
        com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig offlineTransducerModelConfig3 = offlineTransducerModelConfig2;
        com.k2fsa.sherpa.onnx.OfflineFireRedAsrCtcModelConfig offlineFireRedAsrCtcModelConfig2 = (i2 & 16384) != 0 ? offlineModelConfig.fireRedAsrCtc : offlineFireRedAsrCtcModelConfig;
        com.k2fsa.sherpa.onnx.OfflineCanaryModelConfig offlineCanaryModelConfig2 = (i2 & 32768) != 0 ? offlineModelConfig.canary : offlineCanaryModelConfig;
        com.k2fsa.sherpa.onnx.OfflineCohereTranscribeModelConfig offlineCohereTranscribeModelConfig2 = (i2 & 65536) != 0 ? offlineModelConfig.cohereTranscribe : offlineCohereTranscribeModelConfig;
        java.lang.String str9 = (i2 & 131072) != 0 ? offlineModelConfig.teleSpeech : str;
        int i3 = (i2 & 262144) != 0 ? offlineModelConfig.numThreads : i;
        boolean z2 = (i2 & 524288) != 0 ? offlineModelConfig.debug : z;
        java.lang.String str10 = (i2 & 1048576) != 0 ? offlineModelConfig.provider : str2;
        java.lang.String str11 = (i2 & 2097152) != 0 ? offlineModelConfig.modelType : str3;
        java.lang.String str12 = (i2 & 4194304) != 0 ? offlineModelConfig.tokens : str4;
        java.lang.String str13 = (i2 & 8388608) != 0 ? offlineModelConfig.modelingUnit : str5;
        if ((i2 & io.github.mangi.eta.agent.roleplay.CharacterCardCodec.MAX_CARD_BYTES) != 0) {
            str8 = str13;
            str7 = offlineModelConfig.bpeVocab;
        } else {
            str7 = str6;
            str8 = str13;
        }
        return offlineModelConfig.copy(offlineTransducerModelConfig3, offlineParaformerModelConfig2, offlineWhisperModelConfig2, offlineFireRedAsrModelConfig2, offlineMoonshineModelConfig2, offlineNemoEncDecCtcModelConfig2, offlineSenseVoiceModelConfig2, offlineDolphinModelConfig2, offlineZipformerCtcModelConfig2, offlineWenetCtcModelConfig2, offlineOmnilingualAsrCtcModelConfig2, offlineMedAsrCtcModelConfig2, offlineFunAsrNanoModelConfig2, offlineQwen3AsrModelConfig2, offlineFireRedAsrCtcModelConfig2, offlineCanaryModelConfig2, offlineCohereTranscribeModelConfig2, str9, i3, z2, str10, str11, str12, str8, str7);
    }

    /* JADX INFO: renamed from: component1, reason: from getter */
    public final com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig getTransducer() {
        return this.transducer;
    }

    /* JADX INFO: renamed from: component10, reason: from getter */
    public final com.k2fsa.sherpa.onnx.OfflineWenetCtcModelConfig getWenetCtc() {
        return this.wenetCtc;
    }

    /* JADX INFO: renamed from: component11, reason: from getter */
    public final com.k2fsa.sherpa.onnx.OfflineOmnilingualAsrCtcModelConfig getOmnilingual() {
        return this.omnilingual;
    }

    /* JADX INFO: renamed from: component12, reason: from getter */
    public final com.k2fsa.sherpa.onnx.OfflineMedAsrCtcModelConfig getMedasr() {
        return this.medasr;
    }

    /* JADX INFO: renamed from: component13, reason: from getter */
    public final com.k2fsa.sherpa.onnx.OfflineFunAsrNanoModelConfig getFunasrNano() {
        return this.funasrNano;
    }

    /* JADX INFO: renamed from: component14, reason: from getter */
    public final com.k2fsa.sherpa.onnx.OfflineQwen3AsrModelConfig getQwen3Asr() {
        return this.qwen3Asr;
    }

    /* JADX INFO: renamed from: component15, reason: from getter */
    public final com.k2fsa.sherpa.onnx.OfflineFireRedAsrCtcModelConfig getFireRedAsrCtc() {
        return this.fireRedAsrCtc;
    }

    /* JADX INFO: renamed from: component16, reason: from getter */
    public final com.k2fsa.sherpa.onnx.OfflineCanaryModelConfig getCanary() {
        return this.canary;
    }

    /* JADX INFO: renamed from: component17, reason: from getter */
    public final com.k2fsa.sherpa.onnx.OfflineCohereTranscribeModelConfig getCohereTranscribe() {
        return this.cohereTranscribe;
    }

    /* JADX INFO: renamed from: component18, reason: from getter */
    public final java.lang.String getTeleSpeech() {
        return this.teleSpeech;
    }

    /* JADX INFO: renamed from: component19, reason: from getter */
    public final int getNumThreads() {
        return this.numThreads;
    }

    /* JADX INFO: renamed from: component2, reason: from getter */
    public final com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig getParaformer() {
        return this.paraformer;
    }

    /* JADX INFO: renamed from: component20, reason: from getter */
    public final boolean getDebug() {
        return this.debug;
    }

    /* JADX INFO: renamed from: component21, reason: from getter */
    public final java.lang.String getProvider() {
        return this.provider;
    }

    /* JADX INFO: renamed from: component22, reason: from getter */
    public final java.lang.String getModelType() {
        return this.modelType;
    }

    /* JADX INFO: renamed from: component23, reason: from getter */
    public final java.lang.String getTokens() {
        return this.tokens;
    }

    /* JADX INFO: renamed from: component24, reason: from getter */
    public final java.lang.String getModelingUnit() {
        return this.modelingUnit;
    }

    /* JADX INFO: renamed from: component25, reason: from getter */
    public final java.lang.String getBpeVocab() {
        return this.bpeVocab;
    }

    /* JADX INFO: renamed from: component3, reason: from getter */
    public final com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig getWhisper() {
        return this.whisper;
    }

    /* JADX INFO: renamed from: component4, reason: from getter */
    public final com.k2fsa.sherpa.onnx.OfflineFireRedAsrModelConfig getFireRedAsr() {
        return this.fireRedAsr;
    }

    /* JADX INFO: renamed from: component5, reason: from getter */
    public final com.k2fsa.sherpa.onnx.OfflineMoonshineModelConfig getMoonshine() {
        return this.moonshine;
    }

    /* JADX INFO: renamed from: component6, reason: from getter */
    public final com.k2fsa.sherpa.onnx.OfflineNemoEncDecCtcModelConfig getNemo() {
        return this.nemo;
    }

    /* JADX INFO: renamed from: component7, reason: from getter */
    public final com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig getSenseVoice() {
        return this.senseVoice;
    }

    /* JADX INFO: renamed from: component8, reason: from getter */
    public final com.k2fsa.sherpa.onnx.OfflineDolphinModelConfig getDolphin() {
        return this.dolphin;
    }

    /* JADX INFO: renamed from: component9, reason: from getter */
    public final com.k2fsa.sherpa.onnx.OfflineZipformerCtcModelConfig getZipformerCtc() {
        return this.zipformerCtc;
    }

    public final com.k2fsa.sherpa.onnx.OfflineModelConfig copy(com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig transducer, com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig paraformer, com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig whisper, com.k2fsa.sherpa.onnx.OfflineFireRedAsrModelConfig fireRedAsr, com.k2fsa.sherpa.onnx.OfflineMoonshineModelConfig moonshine, com.k2fsa.sherpa.onnx.OfflineNemoEncDecCtcModelConfig nemo, com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig senseVoice, com.k2fsa.sherpa.onnx.OfflineDolphinModelConfig dolphin, com.k2fsa.sherpa.onnx.OfflineZipformerCtcModelConfig zipformerCtc, com.k2fsa.sherpa.onnx.OfflineWenetCtcModelConfig wenetCtc, com.k2fsa.sherpa.onnx.OfflineOmnilingualAsrCtcModelConfig omnilingual, com.k2fsa.sherpa.onnx.OfflineMedAsrCtcModelConfig medasr, com.k2fsa.sherpa.onnx.OfflineFunAsrNanoModelConfig funasrNano, com.k2fsa.sherpa.onnx.OfflineQwen3AsrModelConfig qwen3Asr, com.k2fsa.sherpa.onnx.OfflineFireRedAsrCtcModelConfig fireRedAsrCtc, com.k2fsa.sherpa.onnx.OfflineCanaryModelConfig canary, com.k2fsa.sherpa.onnx.OfflineCohereTranscribeModelConfig cohereTranscribe, java.lang.String teleSpeech, int numThreads, boolean debug, java.lang.String provider, java.lang.String modelType, java.lang.String tokens, java.lang.String modelingUnit, java.lang.String bpeVocab) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(transducer, "transducer");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(paraformer, "paraformer");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(whisper, "whisper");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(fireRedAsr, "fireRedAsr");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(moonshine, "moonshine");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(nemo, "nemo");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(senseVoice, "senseVoice");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(dolphin, "dolphin");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(zipformerCtc, "zipformerCtc");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(wenetCtc, "wenetCtc");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(omnilingual, "omnilingual");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(medasr, "medasr");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(funasrNano, "funasrNano");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(qwen3Asr, "qwen3Asr");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(fireRedAsrCtc, "fireRedAsrCtc");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(canary, "canary");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(cohereTranscribe, "cohereTranscribe");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(teleSpeech, "teleSpeech");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(provider, "provider");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(modelType, "modelType");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(tokens, "tokens");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(modelingUnit, "modelingUnit");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(bpeVocab, "bpeVocab");
        return new com.k2fsa.sherpa.onnx.OfflineModelConfig(transducer, paraformer, whisper, fireRedAsr, moonshine, nemo, senseVoice, dolphin, zipformerCtc, wenetCtc, omnilingual, medasr, funasrNano, qwen3Asr, fireRedAsrCtc, canary, cohereTranscribe, teleSpeech, numThreads, debug, provider, modelType, tokens, modelingUnit, bpeVocab);
    }

    public boolean equals(java.lang.Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof com.k2fsa.sherpa.onnx.OfflineModelConfig)) {
            return false;
        }
        com.k2fsa.sherpa.onnx.OfflineModelConfig offlineModelConfig = (com.k2fsa.sherpa.onnx.OfflineModelConfig) other;
        return kotlin.jvm.internal.Intrinsics.areEqual(this.transducer, offlineModelConfig.transducer) && kotlin.jvm.internal.Intrinsics.areEqual(this.paraformer, offlineModelConfig.paraformer) && kotlin.jvm.internal.Intrinsics.areEqual(this.whisper, offlineModelConfig.whisper) && kotlin.jvm.internal.Intrinsics.areEqual(this.fireRedAsr, offlineModelConfig.fireRedAsr) && kotlin.jvm.internal.Intrinsics.areEqual(this.moonshine, offlineModelConfig.moonshine) && kotlin.jvm.internal.Intrinsics.areEqual(this.nemo, offlineModelConfig.nemo) && kotlin.jvm.internal.Intrinsics.areEqual(this.senseVoice, offlineModelConfig.senseVoice) && kotlin.jvm.internal.Intrinsics.areEqual(this.dolphin, offlineModelConfig.dolphin) && kotlin.jvm.internal.Intrinsics.areEqual(this.zipformerCtc, offlineModelConfig.zipformerCtc) && kotlin.jvm.internal.Intrinsics.areEqual(this.wenetCtc, offlineModelConfig.wenetCtc) && kotlin.jvm.internal.Intrinsics.areEqual(this.omnilingual, offlineModelConfig.omnilingual) && kotlin.jvm.internal.Intrinsics.areEqual(this.medasr, offlineModelConfig.medasr) && kotlin.jvm.internal.Intrinsics.areEqual(this.funasrNano, offlineModelConfig.funasrNano) && kotlin.jvm.internal.Intrinsics.areEqual(this.qwen3Asr, offlineModelConfig.qwen3Asr) && kotlin.jvm.internal.Intrinsics.areEqual(this.fireRedAsrCtc, offlineModelConfig.fireRedAsrCtc) && kotlin.jvm.internal.Intrinsics.areEqual(this.canary, offlineModelConfig.canary) && kotlin.jvm.internal.Intrinsics.areEqual(this.cohereTranscribe, offlineModelConfig.cohereTranscribe) && kotlin.jvm.internal.Intrinsics.areEqual(this.teleSpeech, offlineModelConfig.teleSpeech) && this.numThreads == offlineModelConfig.numThreads && this.debug == offlineModelConfig.debug && kotlin.jvm.internal.Intrinsics.areEqual(this.provider, offlineModelConfig.provider) && kotlin.jvm.internal.Intrinsics.areEqual(this.modelType, offlineModelConfig.modelType) && kotlin.jvm.internal.Intrinsics.areEqual(this.tokens, offlineModelConfig.tokens) && kotlin.jvm.internal.Intrinsics.areEqual(this.modelingUnit, offlineModelConfig.modelingUnit) && kotlin.jvm.internal.Intrinsics.areEqual(this.bpeVocab, offlineModelConfig.bpeVocab);
    }

    public int hashCode() {
        return (((((((((((((((((((((((((((((((((((((((((((((((this.transducer.hashCode() * 31) + this.paraformer.hashCode()) * 31) + this.whisper.hashCode()) * 31) + this.fireRedAsr.hashCode()) * 31) + this.moonshine.hashCode()) * 31) + this.nemo.hashCode()) * 31) + this.senseVoice.hashCode()) * 31) + this.dolphin.hashCode()) * 31) + this.zipformerCtc.hashCode()) * 31) + this.wenetCtc.hashCode()) * 31) + this.omnilingual.hashCode()) * 31) + this.medasr.hashCode()) * 31) + this.funasrNano.hashCode()) * 31) + this.qwen3Asr.hashCode()) * 31) + this.fireRedAsrCtc.hashCode()) * 31) + this.canary.hashCode()) * 31) + this.cohereTranscribe.hashCode()) * 31) + this.teleSpeech.hashCode()) * 31) + java.lang.Integer.hashCode(this.numThreads)) * 31) + java.lang.Boolean.hashCode(this.debug)) * 31) + this.provider.hashCode()) * 31) + this.modelType.hashCode()) * 31) + this.tokens.hashCode()) * 31) + this.modelingUnit.hashCode()) * 31) + this.bpeVocab.hashCode();
    }

    public java.lang.String toString() {
        return "OfflineModelConfig(transducer=" + this.transducer + ", paraformer=" + this.paraformer + ", whisper=" + this.whisper + ", fireRedAsr=" + this.fireRedAsr + ", moonshine=" + this.moonshine + ", nemo=" + this.nemo + ", senseVoice=" + this.senseVoice + ", dolphin=" + this.dolphin + ", zipformerCtc=" + this.zipformerCtc + ", wenetCtc=" + this.wenetCtc + ", omnilingual=" + this.omnilingual + ", medasr=" + this.medasr + ", funasrNano=" + this.funasrNano + ", qwen3Asr=" + this.qwen3Asr + ", fireRedAsrCtc=" + this.fireRedAsrCtc + ", canary=" + this.canary + ", cohereTranscribe=" + this.cohereTranscribe + ", teleSpeech=" + this.teleSpeech + ", numThreads=" + this.numThreads + ", debug=" + this.debug + ", provider=" + this.provider + ", modelType=" + this.modelType + ", tokens=" + this.tokens + ", modelingUnit=" + this.modelingUnit + ", bpeVocab=" + this.bpeVocab + ")";
    }

    public OfflineModelConfig(com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig transducer, com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig paraformer, com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig whisper, com.k2fsa.sherpa.onnx.OfflineFireRedAsrModelConfig fireRedAsr, com.k2fsa.sherpa.onnx.OfflineMoonshineModelConfig moonshine, com.k2fsa.sherpa.onnx.OfflineNemoEncDecCtcModelConfig nemo, com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig senseVoice, com.k2fsa.sherpa.onnx.OfflineDolphinModelConfig dolphin, com.k2fsa.sherpa.onnx.OfflineZipformerCtcModelConfig zipformerCtc, com.k2fsa.sherpa.onnx.OfflineWenetCtcModelConfig wenetCtc, com.k2fsa.sherpa.onnx.OfflineOmnilingualAsrCtcModelConfig omnilingual, com.k2fsa.sherpa.onnx.OfflineMedAsrCtcModelConfig medasr, com.k2fsa.sherpa.onnx.OfflineFunAsrNanoModelConfig funasrNano, com.k2fsa.sherpa.onnx.OfflineQwen3AsrModelConfig qwen3Asr, com.k2fsa.sherpa.onnx.OfflineFireRedAsrCtcModelConfig fireRedAsrCtc, com.k2fsa.sherpa.onnx.OfflineCanaryModelConfig canary, com.k2fsa.sherpa.onnx.OfflineCohereTranscribeModelConfig cohereTranscribe, java.lang.String teleSpeech, int numThreads, boolean debug, java.lang.String provider, java.lang.String modelType, java.lang.String tokens, java.lang.String modelingUnit, java.lang.String bpeVocab) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(transducer, "transducer");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(paraformer, "paraformer");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(whisper, "whisper");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(fireRedAsr, "fireRedAsr");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(moonshine, "moonshine");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(nemo, "nemo");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(senseVoice, "senseVoice");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(dolphin, "dolphin");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(zipformerCtc, "zipformerCtc");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(wenetCtc, "wenetCtc");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(omnilingual, "omnilingual");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(medasr, "medasr");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(funasrNano, "funasrNano");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(qwen3Asr, "qwen3Asr");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(fireRedAsrCtc, "fireRedAsrCtc");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(canary, "canary");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(cohereTranscribe, "cohereTranscribe");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(teleSpeech, "teleSpeech");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(provider, "provider");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(modelType, "modelType");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(tokens, "tokens");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(modelingUnit, "modelingUnit");
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(bpeVocab, "bpeVocab");
        this.transducer = transducer;
        this.paraformer = paraformer;
        this.whisper = whisper;
        this.fireRedAsr = fireRedAsr;
        this.moonshine = moonshine;
        this.nemo = nemo;
        this.senseVoice = senseVoice;
        this.dolphin = dolphin;
        this.zipformerCtc = zipformerCtc;
        this.wenetCtc = wenetCtc;
        this.omnilingual = omnilingual;
        this.medasr = medasr;
        this.funasrNano = funasrNano;
        this.qwen3Asr = qwen3Asr;
        this.fireRedAsrCtc = fireRedAsrCtc;
        this.canary = canary;
        this.cohereTranscribe = cohereTranscribe;
        this.teleSpeech = teleSpeech;
        this.numThreads = numThreads;
        this.debug = debug;
        this.provider = provider;
        this.modelType = modelType;
        this.tokens = tokens;
        this.modelingUnit = modelingUnit;
        this.bpeVocab = bpeVocab;
    }

    /* JADX WARN: Illegal instructions before constructor call */
    public /* synthetic */ OfflineModelConfig(com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig offlineTransducerModelConfig, com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig offlineParaformerModelConfig, com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig offlineWhisperModelConfig, com.k2fsa.sherpa.onnx.OfflineFireRedAsrModelConfig offlineFireRedAsrModelConfig, com.k2fsa.sherpa.onnx.OfflineMoonshineModelConfig offlineMoonshineModelConfig, com.k2fsa.sherpa.onnx.OfflineNemoEncDecCtcModelConfig offlineNemoEncDecCtcModelConfig, com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig offlineSenseVoiceModelConfig, com.k2fsa.sherpa.onnx.OfflineDolphinModelConfig offlineDolphinModelConfig, com.k2fsa.sherpa.onnx.OfflineZipformerCtcModelConfig offlineZipformerCtcModelConfig, com.k2fsa.sherpa.onnx.OfflineWenetCtcModelConfig offlineWenetCtcModelConfig, com.k2fsa.sherpa.onnx.OfflineOmnilingualAsrCtcModelConfig offlineOmnilingualAsrCtcModelConfig, com.k2fsa.sherpa.onnx.OfflineMedAsrCtcModelConfig offlineMedAsrCtcModelConfig, com.k2fsa.sherpa.onnx.OfflineFunAsrNanoModelConfig offlineFunAsrNanoModelConfig, com.k2fsa.sherpa.onnx.OfflineQwen3AsrModelConfig offlineQwen3AsrModelConfig, com.k2fsa.sherpa.onnx.OfflineFireRedAsrCtcModelConfig offlineFireRedAsrCtcModelConfig, com.k2fsa.sherpa.onnx.OfflineCanaryModelConfig offlineCanaryModelConfig, com.k2fsa.sherpa.onnx.OfflineCohereTranscribeModelConfig offlineCohereTranscribeModelConfig, java.lang.String str, int i, boolean z, java.lang.String str2, java.lang.String str3, java.lang.String str4, java.lang.String str5, java.lang.String str6, int i2, kotlin.jvm.internal.DefaultConstructorMarker defaultConstructorMarker) {
        com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig offlineTransducerModelConfig2 = (i2 & 1) != 0 ? new com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig(null, null, null, null, 15, null) : offlineTransducerModelConfig;
        com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig offlineParaformerModelConfig2 = (i2 & 2) != 0 ? new com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig(null, null, 3, null) : offlineParaformerModelConfig;
        com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig offlineWhisperModelConfig2 = (i2 & 4) != 0 ? new com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig(null, null, null, null, 0, false, false, 127, null) : offlineWhisperModelConfig;
        com.k2fsa.sherpa.onnx.OfflineFireRedAsrModelConfig offlineFireRedAsrModelConfig2 = (i2 & 8) != 0 ? new com.k2fsa.sherpa.onnx.OfflineFireRedAsrModelConfig(null, null, 3, null) : offlineFireRedAsrModelConfig;
        com.k2fsa.sherpa.onnx.OfflineMoonshineModelConfig offlineMoonshineModelConfig2 = (i2 & 16) != 0 ? new com.k2fsa.sherpa.onnx.OfflineMoonshineModelConfig(null, null, null, null, null, 31, null) : offlineMoonshineModelConfig;
        com.k2fsa.sherpa.onnx.OfflineNemoEncDecCtcModelConfig offlineNemoEncDecCtcModelConfig2 = (i2 & 32) != 0 ? new com.k2fsa.sherpa.onnx.OfflineNemoEncDecCtcModelConfig(null, 1, null) : offlineNemoEncDecCtcModelConfig;
        com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig offlineSenseVoiceModelConfig2 = (i2 & 64) != 0 ? new com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig(null, null, false, null, 15, null) : offlineSenseVoiceModelConfig;
        com.k2fsa.sherpa.onnx.OfflineDolphinModelConfig offlineDolphinModelConfig2 = (i2 & 128) != 0 ? new com.k2fsa.sherpa.onnx.OfflineDolphinModelConfig(null, 1, null) : offlineDolphinModelConfig;
        com.k2fsa.sherpa.onnx.OfflineZipformerCtcModelConfig offlineZipformerCtcModelConfig2 = (i2 & io.github.mangi.eta.agent.voice.local.LocalSpeechVoskArchive.MAX_ENTRIES) != 0 ? new com.k2fsa.sherpa.onnx.OfflineZipformerCtcModelConfig(null, null, 3, null) : offlineZipformerCtcModelConfig;
        com.k2fsa.sherpa.onnx.OfflineWenetCtcModelConfig offlineWenetCtcModelConfig2 = (i2 & io.github.mangi.eta.agent.web.WebPageContent.MAX_TITLE_CHARS) != 0 ? new com.k2fsa.sherpa.onnx.OfflineWenetCtcModelConfig(null, 1, null) : offlineWenetCtcModelConfig;
        com.k2fsa.sherpa.onnx.OfflineOmnilingualAsrCtcModelConfig offlineOmnilingualAsrCtcModelConfig2 = (i2 & 1024) != 0 ? new com.k2fsa.sherpa.onnx.OfflineOmnilingualAsrCtcModelConfig(null, 1, null) : offlineOmnilingualAsrCtcModelConfig;
        com.k2fsa.sherpa.onnx.OfflineMedAsrCtcModelConfig offlineMedAsrCtcModelConfig2 = (i2 & 2048) != 0 ? new com.k2fsa.sherpa.onnx.OfflineMedAsrCtcModelConfig(null, 1, null) : offlineMedAsrCtcModelConfig;
        com.k2fsa.sherpa.onnx.OfflineFunAsrNanoModelConfig offlineFunAsrNanoModelConfig2 = (i2 & 4096) != 0 ? new com.k2fsa.sherpa.onnx.OfflineFunAsrNanoModelConfig(null, null, null, null, null, null, 0, 0.0f, 0.0f, 0, null, false, null, 8191, null) : offlineFunAsrNanoModelConfig;
        com.k2fsa.sherpa.onnx.OfflineQwen3AsrModelConfig offlineQwen3AsrModelConfig2 = (i2 & io.github.mangi.eta.agent.web.PublicWebSearch.MAX_URL_CHARS) != 0 ? new com.k2fsa.sherpa.onnx.OfflineQwen3AsrModelConfig(null, null, null, null, 0, 0, 0.0f, 0.0f, 0, null, 1023, null) : offlineQwen3AsrModelConfig;
        this(offlineTransducerModelConfig2, offlineParaformerModelConfig2, offlineWhisperModelConfig2, offlineFireRedAsrModelConfig2, offlineMoonshineModelConfig2, offlineNemoEncDecCtcModelConfig2, offlineSenseVoiceModelConfig2, offlineDolphinModelConfig2, offlineZipformerCtcModelConfig2, offlineWenetCtcModelConfig2, offlineOmnilingualAsrCtcModelConfig2, offlineMedAsrCtcModelConfig2, offlineFunAsrNanoModelConfig2, offlineQwen3AsrModelConfig2, (i2 & 16384) != 0 ? new com.k2fsa.sherpa.onnx.OfflineFireRedAsrCtcModelConfig(null, 1, null) : offlineFireRedAsrCtcModelConfig, (i2 & 32768) != 0 ? new com.k2fsa.sherpa.onnx.OfflineCanaryModelConfig(null, null, null, null, false, 31, null) : offlineCanaryModelConfig, (i2 & 65536) != 0 ? new com.k2fsa.sherpa.onnx.OfflineCohereTranscribeModelConfig(null, null, null, false, false, 31, null) : offlineCohereTranscribeModelConfig, (i2 & 131072) != 0 ? "" : str, (i2 & 262144) == 0 ? i : 1, (i2 & 524288) != 0 ? false : z, (i2 & 1048576) != 0 ? "cpu" : str2, (i2 & 2097152) != 0 ? "" : str3, (i2 & 4194304) != 0 ? "" : str4, (i2 & 8388608) != 0 ? "" : str5, (i2 & io.github.mangi.eta.agent.roleplay.CharacterCardCodec.MAX_CARD_BYTES) == 0 ? str6 : "");
    }

    public final com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig getTransducer() {
        return this.transducer;
    }

    public final void setTransducer(com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig offlineTransducerModelConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(offlineTransducerModelConfig, "<set-?>");
        this.transducer = offlineTransducerModelConfig;
    }

    public final com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig getParaformer() {
        return this.paraformer;
    }

    public final void setParaformer(com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig offlineParaformerModelConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(offlineParaformerModelConfig, "<set-?>");
        this.paraformer = offlineParaformerModelConfig;
    }

    public final com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig getWhisper() {
        return this.whisper;
    }

    public final void setWhisper(com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig offlineWhisperModelConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(offlineWhisperModelConfig, "<set-?>");
        this.whisper = offlineWhisperModelConfig;
    }

    public final com.k2fsa.sherpa.onnx.OfflineFireRedAsrModelConfig getFireRedAsr() {
        return this.fireRedAsr;
    }

    public final void setFireRedAsr(com.k2fsa.sherpa.onnx.OfflineFireRedAsrModelConfig offlineFireRedAsrModelConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(offlineFireRedAsrModelConfig, "<set-?>");
        this.fireRedAsr = offlineFireRedAsrModelConfig;
    }

    public final com.k2fsa.sherpa.onnx.OfflineMoonshineModelConfig getMoonshine() {
        return this.moonshine;
    }

    public final void setMoonshine(com.k2fsa.sherpa.onnx.OfflineMoonshineModelConfig offlineMoonshineModelConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(offlineMoonshineModelConfig, "<set-?>");
        this.moonshine = offlineMoonshineModelConfig;
    }

    public final com.k2fsa.sherpa.onnx.OfflineNemoEncDecCtcModelConfig getNemo() {
        return this.nemo;
    }

    public final void setNemo(com.k2fsa.sherpa.onnx.OfflineNemoEncDecCtcModelConfig offlineNemoEncDecCtcModelConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(offlineNemoEncDecCtcModelConfig, "<set-?>");
        this.nemo = offlineNemoEncDecCtcModelConfig;
    }

    public final com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig getSenseVoice() {
        return this.senseVoice;
    }

    public final void setSenseVoice(com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig offlineSenseVoiceModelConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(offlineSenseVoiceModelConfig, "<set-?>");
        this.senseVoice = offlineSenseVoiceModelConfig;
    }

    public final com.k2fsa.sherpa.onnx.OfflineDolphinModelConfig getDolphin() {
        return this.dolphin;
    }

    public final void setDolphin(com.k2fsa.sherpa.onnx.OfflineDolphinModelConfig offlineDolphinModelConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(offlineDolphinModelConfig, "<set-?>");
        this.dolphin = offlineDolphinModelConfig;
    }

    public final com.k2fsa.sherpa.onnx.OfflineZipformerCtcModelConfig getZipformerCtc() {
        return this.zipformerCtc;
    }

    public final void setZipformerCtc(com.k2fsa.sherpa.onnx.OfflineZipformerCtcModelConfig offlineZipformerCtcModelConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(offlineZipformerCtcModelConfig, "<set-?>");
        this.zipformerCtc = offlineZipformerCtcModelConfig;
    }

    public final com.k2fsa.sherpa.onnx.OfflineWenetCtcModelConfig getWenetCtc() {
        return this.wenetCtc;
    }

    public final void setWenetCtc(com.k2fsa.sherpa.onnx.OfflineWenetCtcModelConfig offlineWenetCtcModelConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(offlineWenetCtcModelConfig, "<set-?>");
        this.wenetCtc = offlineWenetCtcModelConfig;
    }

    public final com.k2fsa.sherpa.onnx.OfflineOmnilingualAsrCtcModelConfig getOmnilingual() {
        return this.omnilingual;
    }

    public final void setOmnilingual(com.k2fsa.sherpa.onnx.OfflineOmnilingualAsrCtcModelConfig offlineOmnilingualAsrCtcModelConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(offlineOmnilingualAsrCtcModelConfig, "<set-?>");
        this.omnilingual = offlineOmnilingualAsrCtcModelConfig;
    }

    public final com.k2fsa.sherpa.onnx.OfflineMedAsrCtcModelConfig getMedasr() {
        return this.medasr;
    }

    public final void setMedasr(com.k2fsa.sherpa.onnx.OfflineMedAsrCtcModelConfig offlineMedAsrCtcModelConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(offlineMedAsrCtcModelConfig, "<set-?>");
        this.medasr = offlineMedAsrCtcModelConfig;
    }

    public final com.k2fsa.sherpa.onnx.OfflineFunAsrNanoModelConfig getFunasrNano() {
        return this.funasrNano;
    }

    public final void setFunasrNano(com.k2fsa.sherpa.onnx.OfflineFunAsrNanoModelConfig offlineFunAsrNanoModelConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(offlineFunAsrNanoModelConfig, "<set-?>");
        this.funasrNano = offlineFunAsrNanoModelConfig;
    }

    public final com.k2fsa.sherpa.onnx.OfflineQwen3AsrModelConfig getQwen3Asr() {
        return this.qwen3Asr;
    }

    public final void setQwen3Asr(com.k2fsa.sherpa.onnx.OfflineQwen3AsrModelConfig offlineQwen3AsrModelConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(offlineQwen3AsrModelConfig, "<set-?>");
        this.qwen3Asr = offlineQwen3AsrModelConfig;
    }

    public final com.k2fsa.sherpa.onnx.OfflineFireRedAsrCtcModelConfig getFireRedAsrCtc() {
        return this.fireRedAsrCtc;
    }

    public final void setFireRedAsrCtc(com.k2fsa.sherpa.onnx.OfflineFireRedAsrCtcModelConfig offlineFireRedAsrCtcModelConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(offlineFireRedAsrCtcModelConfig, "<set-?>");
        this.fireRedAsrCtc = offlineFireRedAsrCtcModelConfig;
    }

    public final com.k2fsa.sherpa.onnx.OfflineCanaryModelConfig getCanary() {
        return this.canary;
    }

    public final void setCanary(com.k2fsa.sherpa.onnx.OfflineCanaryModelConfig offlineCanaryModelConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(offlineCanaryModelConfig, "<set-?>");
        this.canary = offlineCanaryModelConfig;
    }

    public final com.k2fsa.sherpa.onnx.OfflineCohereTranscribeModelConfig getCohereTranscribe() {
        return this.cohereTranscribe;
    }

    public final void setCohereTranscribe(com.k2fsa.sherpa.onnx.OfflineCohereTranscribeModelConfig offlineCohereTranscribeModelConfig) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(offlineCohereTranscribeModelConfig, "<set-?>");
        this.cohereTranscribe = offlineCohereTranscribeModelConfig;
    }

    public final java.lang.String getTeleSpeech() {
        return this.teleSpeech;
    }

    public final void setTeleSpeech(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.teleSpeech = str;
    }

    public final int getNumThreads() {
        return this.numThreads;
    }

    public final void setNumThreads(int i) {
        this.numThreads = i;
    }

    public final boolean getDebug() {
        return this.debug;
    }

    public final void setDebug(boolean z) {
        this.debug = z;
    }

    public final java.lang.String getProvider() {
        return this.provider;
    }

    public final void setProvider(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.provider = str;
    }

    public final java.lang.String getModelType() {
        return this.modelType;
    }

    public final void setModelType(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.modelType = str;
    }

    public final java.lang.String getTokens() {
        return this.tokens;
    }

    public final void setTokens(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.tokens = str;
    }

    public final java.lang.String getModelingUnit() {
        return this.modelingUnit;
    }

    public final void setModelingUnit(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.modelingUnit = str;
    }

    public final java.lang.String getBpeVocab() {
        return this.bpeVocab;
    }

    public final void setBpeVocab(java.lang.String str) {
        kotlin.jvm.internal.Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.bpeVocab = str;
    }
}
