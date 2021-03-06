package sistemaponto

import org.joda.time.LocalDate
import org.joda.time.LocalTime
import util.UtilitarioSpring

class RegistroPontoService {

    boolean registrar() {
        Funcionario funcionario = UtilitarioSpring.getUsuarioLogado()
        return registrar(funcionario, new LocalDate(), new LocalTime())
    }

    boolean registrar(Funcionario funcionario) {
        return registrar(funcionario, new LocalDate(), new LocalTime())
    }

    boolean registrar(Funcionario funcionario, LocalDate data, LocalTime hora) {
        String queryFilter = "SELECT MAX(id) FROM RegistroPonto WHERE funcionario.id = ? AND dia = ?"
        String hql = "SELECT isEntrada FROM RegistroPonto WHERE id = (${queryFilter})"
        List isEntrada = RegistroPonto.executeQuery(hql,[funcionario.id, data])
        RegistroPonto registroPonto = new RegistroPonto()
        registroPonto.funcionario = funcionario
        registroPonto.dia = data
        registroPonto.hora = hora
        registroPonto.isEntrada = isEntrada.size() > 0 ? !isEntrada.get(0) : true
        return registroPonto.save(flush: true) != null
    }

    void removerTodosPontosDoDia(LocalDate dia, Funcionario funcionario) {
        List<RegistroPonto> pontos = RegistroPonto.findAllByDiaAndFuncionario(dia, funcionario)
        for (ponto in pontos) {
            ponto.delete()
        }
    }

    void marcarRequisicaoNoDia(LocalDate dia, Funcionario funcionario){
        List<RegistroPonto> pontos = RegistroPonto.findAllByDiaAndFuncionario(dia, funcionario)
        for(ponto in pontos){
            ponto.temRequisicao = true
            ponto.save()
        }
    }

    List<RegistroPonto> buscarPontosDoDia(LocalDate dia){
        return RegistroPonto.findAllByDia(dia)
    }

//    List<Dia> listaPontosMesCorrente(long idFuncionario) {
//
//        List<RegistroPonto> pontos = (List<RegistroPonto>) RegistroPonto.withCriteria {
//            eq('funcionario.id', idFuncionario)
//            gt('dia', ConfiguracaoService.getUltimoDiaFechamento())
//            order('dia', 'desc')
//        }
//        List<Dia> dias = []
//        pontos.groupBy { it.dia.get(DateTimeFieldType.dayOfMonth()) }
//                .each { pontosDoDia ->
//
//            Dia objetoDia = new Dia()
//            objetoDia.data = pontosDoDia.value.get(0).dia
//            pontosDoDia.value.each { ponto ->
//                objetoDia.pontos.add(ponto)
//            }
//            objetoDia.pontos.sort{it.hora}
//            dias.add(objetoDia)
//            dias = dias.sort { it.data }
//        }
//        return dias
//    }
}
