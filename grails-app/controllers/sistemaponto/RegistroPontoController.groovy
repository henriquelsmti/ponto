package sistemaponto

import entity.FormatadorDataHora
import grails.plugin.springsecurity.annotation.Secured
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import util.UtilitarioSpring

@Secured(['ROLE_USER', 'ROLE_ADMIN'])
class RegistroPontoController {


    RegistroPontoService registroPontoService

    def baterPonto() {
        Funcionario funcionario = UtilitarioSpring.getUsuarioLogado()
        Map model = [:]
        if (registroPontoService.bater(funcionario)) {
            model['msg'] = "Ponto registrado com sucesso"
            return forward(controller: 'funcionario', action: 'home', model: model)
        }
        model['msg'] = "Falha ao registrar o ponto"
        return forward(controller: 'funcionario', action: 'home', model: model)
    }

    @Secured(['permitAll'])
    def baterPontoMaquina() {
        Funcionario funcionario = Funcionario.get(params.id as int)
        if (funcionario) {
            LocalDate data = new LocalDate()
            LocalTime hora = new LocalTime()
            String mensagem = ""
            if (registroPontoService.bater(funcionario, data, hora)) {
                mensagem = "Ponto registrado para o funcionário ${funcionario.nome}"
            } else {
                mensagem = "Falha ao registrar ponto para o funcionário ${funcionario.nome}"
            }
            return render(contentType: "application/json") {
                resultado(msg: mensagem,
                horario: "${FormatadorDataHora.toDateTime(data, hora)}")
            }
        }
        return render(status: 404, contentType: "application/json") {
            resultado(msg: "Funcionário não encontrado")
        }
    }
}