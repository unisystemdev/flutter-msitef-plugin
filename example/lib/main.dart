import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'dart:async';

import 'package:flutter_msitef_plugin/flutter_msitef_plugin.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      home: IntentForm(),
    );
  }
}

class IntentForm extends StatefulWidget {
  const IntentForm({super.key});

  @override
  _IntentFormState createState() => _IntentFormState();
}

class _IntentFormState extends State<IntentForm> {
  final _flutterMsitefPlugin = FlutterMsitefPlugin();
  final _formKey = GlobalKey<FormState>();

  String tipoPinpad = "";
  String tipoParcelamento = "NENHUM";

  final Map<String, TextEditingController> controllers = {
    "empresaSitef": TextEditingController(text: "00000000"),
    "enderecoSitef": TextEditingController(text: "192.168.1.100"),
    "CNPJ_CPF": TextEditingController(text: "00000000000000"),
    "cnpj_automacao": TextEditingController(text: "68884084000107"),
    "comExterna": TextEditingController(text: "0"),
    "otp": TextEditingController(text: ""),
    "tokenRegistroTls": TextEditingController(text: ""),
    "operador": TextEditingController(text: "0001"),
    "numeroCupom": TextEditingController(text: "1"),
    "valor": TextEditingController(text: "9000"),
    "numParcelas": TextEditingController(text: "1"),
    "timeoutColeta": TextEditingController(text: "30"),
  };

  Map<String, String> getFormValues() {
    final formValues =
        controllers.map((key, controller) => MapEntry(key, controller.text));
    formValues['tipoPinpad'] = tipoPinpad;
    formValues['tipoParcelamento'] = tipoParcelamento;
    return formValues;
  }

  Future<void> msitefSuccess(MSitefResponse response) {
    final String message = 'CODRESP: ${response.codresp} \n\n'
        'CODTRANS: ${response.codtrans} \n\n'
        'TIPO_PARC: ${response.tipoParc} \n\n'
        'REDE_AUT: ${response.redeAut} \n\n'
        'BANDEIRA: ${response.bandeira} \n\n'
        'NSU_SITEF: ${response.nsuSitef} \n\n'
        'NSU_HOST: ${response.nsuHost} \n\n'
        'NUM_PARC: ${response.numParc}';

    showToast("SUCCESS", message);

    return Future.value();
  }

  Future<void> msitefFail(MSitefResponseFail response) {
    final message =
        "Resultado m-SiTef FAIL: ${response.codresp} - ${response.message} ";
    showToast('ERROR', message);

    return Future.value();
  }

  void msitefAdm() async {
    final formData = getFormValues();
    await _flutterMsitefPlugin.msitefAdm(
      params: formData,
      callback: msitefSuccess,
      callbackFail: msitefFail,
    );
  }

  void msitefVendaCredito() async {
    final formData = getFormValues();
    await _flutterMsitefPlugin.msitefCredito(
      params: formData,
      callback: msitefSuccess,
      callbackFail: msitefFail,
    );
  }

  void msitefVendaDebito() async {
    final formData = getFormValues();
    await _flutterMsitefPlugin.msitefDebito(
      params: formData,
      callback: msitefSuccess,
      callbackFail: msitefFail,
    );
  }

  void msitefPix() async {
    final formData = getFormValues();
    await _flutterMsitefPlugin.msitefPix(
      params: formData,
      callback: msitefSuccess,
      callbackFail: msitefFail,
    );
  }

  void msitefCancel() async {
    final formData = getFormValues();
    await _flutterMsitefPlugin.msitefCancelamento(
      params: formData,
      callback: msitefSuccess,
      callbackFail: msitefFail,
    );
  }

  void executeWithExceptionHandling(Function action) {
    try {
      action();
    } catch (e) {
      showToast('ERROR', '$e');
    }
  }

  void showToast(String tipo, String message) {
    Color backgroundColor;
    ToastGravity gravity;

    switch (tipo) {
      case 'SUCCESS':
        backgroundColor = Colors.green;
        gravity = ToastGravity.CENTER;
        break;
      case 'ERROR':
        backgroundColor = Colors.red;
        gravity = ToastGravity.BOTTOM;
        break;
      default:
        backgroundColor =
            Colors.grey; // Default color if 'tipo' is not recognized
        gravity = ToastGravity.TOP;
    }

    Fluttertoast.showToast(
      msg: message,
      toastLength: Toast.LENGTH_LONG,
      gravity: gravity,
      backgroundColor: backgroundColor,
      textColor: Colors.white,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('mSitef: demo'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Form(
          key: _formKey,
          child: SingleChildScrollView(
            child: Column(
              children: [
                ...controllers.entries.map((entry) {
                  return TextFormField(
                    controller: entry.value,
                    decoration: InputDecoration(labelText: entry.key),
                  );
                }),
                DropdownButtonFormField<String>(
                  value: tipoPinpad,
                  decoration: const InputDecoration(labelText: "tipoPinpad"),
                  items: ["", "ANDROID_USB", "ANDROID_BT"].map((String value) {
                    return DropdownMenuItem<String>(
                      value: value,
                      child: Text(value),
                    );
                  }).toList(),
                  onChanged: (newValue) {
                    setState(() {
                      tipoPinpad = newValue!;
                    });
                  },
                ),
                DropdownButtonFormField<String>(
                  value: tipoParcelamento,
                  decoration:
                      const InputDecoration(labelText: "tipoParcelamento"),
                  items: ["NENHUM", "LOJA", "ADM"].map((String value) {
                    return DropdownMenuItem<String>(
                      value: value,
                      child: Text(value),
                    );
                  }).toList(),
                  onChanged: (newValue) {
                    setState(() {
                      tipoParcelamento = newValue!;
                    });
                  },
                ),
                const SizedBox(height: 20),
                ElevatedButton(
                  onPressed: () => executeWithExceptionHandling(msitefAdm),
                  child: const Text('ADM'),
                ),
                ElevatedButton(
                  onPressed: () =>
                      executeWithExceptionHandling(msitefVendaCredito),
                  child: const Text('VendaCredito'),
                ),
                ElevatedButton(
                  onPressed: () =>
                      executeWithExceptionHandling(msitefVendaDebito),
                  child: const Text('VendaDebito'),
                ),
                ElevatedButton(
                  onPressed: () => executeWithExceptionHandling(msitefPix),
                  child: const Text('Pix'),
                ),
                ElevatedButton(
                  onPressed: () => executeWithExceptionHandling(msitefCancel),
                  child: const Text('Cancelamento'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    controllers.forEach((key, controller) {
      controller.dispose();
    });
    super.dispose();
  }
}
