package com.example.flutter_msitef_plugin

import android.app.Activity
import android.content.Intent
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import org.json.JSONObject
import android.util.Log

enum class TipoProcessamento {
  OUTROS,
  CREDITO,
  DEBITO,
  PIX,
  ADM
}

class FlutterMsitefPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  private lateinit var channel : MethodChannel
  private var activity: Activity? = null
  private val REQUEST_CODE_MSITEF = 1234
  private val REQUEST_URL_MSITEF = "br.com.softwareexpress.sitef.msitef.ACTIVITY_CLISITEF"

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_msitef_plugin")
    channel.setMethodCallHandler(this)
    Log.d("KOTLIN", "onAttachedToEngine")
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    Log.d("KOTLIN", "onMethodCall")

    when (call.method) {

      "msitef#getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
      
      "msitef#credito" -> handleSitefCredito(call, result)

      "msitef#adm" -> handleSitefAdm(call, result)
      
    else -> result.notImplemented()
    
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    Log.d("KOTLIN", "onDetachedFromEngine")
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    Log.d("KOTLIN", "onAttachedToActivity")

    activity = binding.activity
    binding.addActivityResultListener { requestCode, resultCode, data ->
        Log.d("KOTLIN", "onActivityResultListener")
        
        if (requestCode == REQUEST_CODE_MSITEF) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    Log.d("KOTLIN", "Activity RESULT_OK")
                    val response = mapOf(
                        "STATUS" to "RESULT_OK",                        
                        "CODRESP" to data?.getStringExtra("CODRESP"),
                        "COMP_DADOS_CONF" to data?.getStringExtra("COMP_DADOS_CONF"),
                        "CODTRANS" to data?.getStringExtra("CODTRANS"),
                        "TIPO_PARC" to data?.getStringExtra("TIPO_PARC"),
                        "VLTROCO" to data?.getStringExtra("VLTROCO"),
                        "REDE_AUT" to data?.getStringExtra("REDE_AUT"),
                        "BANDEIRA" to data?.getStringExtra("BANDEIRA"),
                        "NSU_SITEF" to data?.getStringExtra("NSU_SITEF"),
                        "NSU_HOST" to data?.getStringExtra("NSU_HOST"),
                        "COD_AUTORIZACAO" to data?.getStringExtra("COD_AUTORIZACAO"),
                        "NUM_PARC" to data?.getStringExtra("NUM_PARC"),
                        "VIA_ESTABELECIMENTO" to data?.getStringExtra("VIA_ESTABELECIMENTO"),
                        "VIA_CLIENTE" to data?.getStringExtra("VIA_CLIENTE"),
                        "TIPO_CAMPOS" to data?.getStringExtra("TIPO_CAMPOS")
                    )
                    channel.invokeMethod("onMsitefResult", response)
                }
                Activity.RESULT_CANCELED -> {
                    Log.d("KOTLIN", "Activity RESULT_CANCELED")
                    val codResp = data?.getStringExtra("CODRESP")
                    var message: String = "Código de resposta desconhecido"
                    
                    if (codResp != null) {
                      message = getErrorDescription( codResp )
                    }
                    else {
                      message = "msitef: CODRESP inválido"
                    }

                    val response = mapOf(
                      "STATUS" to "RESULT_CANCELED",
                      "CODRESP" to data?.getStringExtra("CODRESP"),
                      "MESSAGE" to message
                    )
                    
                    channel.invokeMethod("onMsitefResult", response)                    
                }
                else -> {
                    Log.d("KOTLIN", "Activity resultCode: $resultCode")
                    
                    val codResp = data?.getStringExtra("CODRESP")
                    var message: String = "Código de resposta desconhecido"
                    
                    if (codResp != null) {
                      message = getErrorDescription( codResp )
                    }
                    else {
                      message = "msitef: CODRESP inválido"
                    }

                    val response = mapOf(
                        "STATUS" to "RESULT_OTHER",
                        "CODRESP" to "-1",
                        "MESSAGE" to message
                      )
                    
                    channel.invokeMethod("onMsitefResult", response)
                }
            }
            true
        } else {
            false
        }
    }
  }

  private fun handleSitefCredito(call: MethodCall, result: Result) {
    Log.d("KOTLIN", "handleSitefCredito")
    // val intent =  getIntent(TipoProcessamento.CREDITO, call)

    // activity?.startActivityForResult(intent, REQUEST_CODE_MSITEF)

    result.success(null)
  }

  private fun handleSitefAdm(call: MethodCall, result: Result) {
    Log.d("KOTLIN", "handleSitefAdm")
    val intent =  getIntent(TipoProcessamento.ADM, call)
    activity?.startActivityForResult(intent, REQUEST_CODE_MSITEF)
    result.success(null)
  }

  override fun onDetachedFromActivity() {
    Log.d("KOTLIN", "onDetachedFromActivity")
    activity = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    Log.d("KOTLIN", "onReattachedToActivityForConfigChanges")
    activity = binding.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {
    Log.d("KOTLIN", "onDetachedFromActivityForConfigChanges")
    activity = null
  }


  private fun getIntent(tipoProcessamento: TipoProcessamento, call: MethodCall): Intent? {
    val intent = Intent(REQUEST_URL_MSITEF)
    
    intent.putExtra("empresaSitef", call.argument<String>("empresaSitef"))
    intent.putExtra("enderecoSitef", call.argument<String>("enderecoSitef"))
    intent.putExtra("CNPJ_CPF", call.argument<String>("CNPJ_CPF"))
    intent.putExtra("comExterna", call.argument<String>("comExterna"))
    // intent.putExtra("timeoutColeta", "30")
    // intent.putExtra("tipoPinpad", "ANDROID_USB")
        
    if (tipoProcessamento == TipoProcessamento.ADM) {
      intent.putExtra("modalidade", "110");
    }
    else{
      intent.putExtra("operador", call.argument<String>("operador"))
      intent.putExtra("data", call.argument<String>("data"))
      intent.putExtra("hora", call.argument<String>("hora"))
      intent.putExtra("numeroCupom", call.argument<String>("numeroCupom"))
      intent.putExtra("numParcelas", "1")
      intent.putExtra("valor", call.argument<String>("valor"))

      if (tipoProcessamento == TipoProcessamento.OUTROS) { 
        intent.putExtra("modalidade", "0")
      }
    
      if (tipoProcessamento == TipoProcessamento.DEBITO) {
        intent.putExtra("restricoes", "TransacoesHabilitadas=16")
        intent.putExtra("modalidade", "2")
      }
  
      if (tipoProcessamento == TipoProcessamento.CREDITO) {  
        intent.putExtra("restricoes", "TransacoesHabilitadas=26")
        intent.putExtra("modalidade", "3")
      }
      
      if (tipoProcessamento == TipoProcessamento.PIX) {        
        intent.putExtra("restricoes", "CarteirasDigitaisHabilitadas=027160110024");
        intent.putExtra("transacoesHabilitadas", "7;8;");
        intent.putExtra("modalidade", "122")
      }
    }

    return intent
  }

  fun getErrorDescription(codResp: String): String {
    return when (codResp.toIntOrNull()) {
        0 -> "Sucesso na execução da função."
        1 -> "Endereço IP inválido ou não resolvido"
        2 -> "Código da loja inválido"
        3 -> "Código de terminal inválido"
        6 -> "Erro na inicialização do Tcp/Ip"
        7 -> "Falta de memória"
        8 -> "Não encontrou a CliSiTef ou ela está com problemas"
        9 -> "Configuração de servidores SiTef foi excedida."
        10 -> "Erro de acesso na pasta CliSiTef (possível falta de permissão para escrita)"
        11 -> "Dados inválidos passados pela automação."
        12 -> "Modo seguro não ativo"
        13 -> "Caminho DLL inválido (o caminho completo das bibliotecas está muito grande)."
        in 14..Int.MAX_VALUE -> "Negada pelo autorizador."
        -1 -> "Módulo não inicializado. O PDV tentou chamar alguma rotina sem antes executar a função configura."
        -2 -> "Operação cancelada pelo operador."
        -3 -> "O parâmetro função / modalidade é inexistente/inválido."
        -4 -> "Falta de memória no PDV."
        -5 -> "Sem comunicação com o SiTef."
        -6 -> "Operação cancelada pelo usuário (no pinpad)."
        -9 -> "A automação chamou a rotina ContinuaFuncaoSiTefInterativo sem antes iniciar uma função iterativa."
        -10 -> "Algum parâmetro obrigatório não foi passado pela automação comercial."
        -12 -> "Erro na execução da rotina iterativa. Provavelmente o processo iterativo anterior não foi executado até o final (enquanto o retorno for igual a 10000)."
        -13 -> "Documento fiscal não encontrado nos registros da CliSiTef. Retornado em funções de consulta tais como ObtemQuantidadeTransaçõesPendentes."
        -15 -> "Operação cancelada pela automação comercial."
        -20 -> "Parâmetro inválido passado para a função."
        -25 -> "Erro no Correspondente Bancário: Deve realizar sangria."
        -30 -> "Erro de acesso ao arquivo. Certifique-se que o usuário que roda a aplicação tem direitos de leitura/escrita."
        -40 -> "Transação negada pelo servidor SiTef."
        -41 -> "Dados inválidos."
        -43 -> "Problema na execução de alguma das rotinas no pinpad."
        -50 -> "Transação não segura."
        -100 -> "Erro interno do módulo."
        in Int.MIN_VALUE..-101 -> "Erros detectados internamente pela rotina."
        else -> "Código de resposta desconhecido."
    }
}
}
