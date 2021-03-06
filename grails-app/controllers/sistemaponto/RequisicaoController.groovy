package sistemaponto

import grails.plugin.springsecurity.annotation.Secured
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import util.UtilitarioSpring

import java.text.SimpleDateFormat

@Secured(['ROLE_ADMIN'])
class RequisicaoController {

    static defaultAction = "formulario"
    RequisicaoService requisicaoService

    @Secured(['ROLE_USER'])
    def formulario() {
        render(view: 'formulario', model: [data: params.data])
    }

    @Secured(['ROLE_USER'])
    def requisitar() {

        Map model = [:]
        if (!params.diaRequisitado) {
            model['msg'] = "Data não informada."
            render(view: 'index', model: model)
            return
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy")
        Date data
        model['data'] = params.diaRequisitado
        try {
            data = sdf.parse(params.diaRequisitado as String)
        } catch (Exception ex) {
            model['msg'] = "Formato de data inválido."
            render(view: 'index', model: model)
            return
        }
        if (!params.horarios || params.horarios.size() != 4) {
            model['msg'] = "Informe todos os horários."
            render(view: 'index', model: model)
            return
        }
        List<RequisicaoHorario> horarios = []
        try {
            params.horarios.each {horario ->
                horarios << createRequisicaoHorario(horario.value as String)
            }
        } catch (Exception ex) {
            model['msg'] = "Algum horário está com formato inválido."
            render(view: 'index', model: model)
            return
        }

        Requisicao requisicao = new Requisicao()
        requisicao.funcionario = UtilitarioSpring.getUsuarioLogado()
        requisicao.dataSolicitacao = new LocalDate()
        requisicao.diaRequisitado = LocalDate.fromDateFields(data)
        requisicao.justificativa = params.justificativa
        for(reqHorario in horarios){
            requisicao.addToHorarios(reqHorario)
        }

        if (requisicaoService.criarRequisicao(requisicao)) {
            model['msg'] = "Requisição enviada com sucesso, aguarde a avaliação do administrador"
        } else {
            model['msg'] = "Falha ao requisitar ponto passado. "
            requisicao.errors.allErrors.each { model['msg'] += it }
        }
        chain(controller: 'funcionario', action: 'homepadrao', model: model)
    }

    def listar() {
        render(view: 'lista', model: ['requisicoes': requisicaoService.listarEmAberto()])
    }

    def detalhe() {
        Map model = [:]
        Requisicao requisicao = requisicaoService.get(Integer.parseInt(params.id as String))
        model.requisicao = requisicao
        model.horarios = convertSetToList(requisicao.horarios)
        render(view: 'detalhe', model: model)
    }

    def aprovar() {
        boolean result = requisicaoService.finalizar(true, params.id as int)
        preparaMsgEEncaminhaPraView("aprova", result)
    }

    def recusar() {
        boolean cadastrou = requisicaoService.finalizar(false, params.id as int)
        preparaMsgEEncaminhaPraView("recusa", cadastrou)
    }

    private def preparaMsgEEncaminhaPraView(String aprovadoOuRecusado, boolean success) {
        Map model = [:]
        if (success) {
            model.msg = "Requisição ${aprovadoOuRecusado}da com sucesso"
        } else {
            model.msg = "Falha ao ${aprovadoOuRecusado}r requisição"
        }
        return forward(action: 'listar', model: model)
    }

    private static RequisicaoHorario createRequisicaoHorario(String horario) {
        println "HORARIO:::${horario}"
        String[] strNumeros = horario.split(":")
        RequisicaoHorario rh = new RequisicaoHorario()
        rh.horario = new LocalTime(strNumeros[0] as int, strNumeros[1] as int, strNumeros[2] as int)
        return rh
    }

    private static List<LocalTime> convertSetToList(Set<RequisicaoHorario> horariosSet) {
        List<LocalTime> horariosList = []
        for(set in horariosSet){
            horariosList.add(set.horario)
        }
        return horariosList
    }

}
