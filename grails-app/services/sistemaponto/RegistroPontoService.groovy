package sistemaponto

import entity.ConfiguracaoService
import entity.Dia
import org.joda.time.DateTimeFieldType
import org.joda.time.LocalDate
import org.joda.time.LocalTime

class RegistroPontoService {

    boolean bater(Funcionario funcionario) {
        return bater(funcionario, new LocalDate(), new LocalTime())
    }

    boolean bater(Funcionario funcionario, LocalDate data, LocalTime hora) {
        String queryFilter = "SELECT MAX(id) FROM RegistroPonto WHERE funcionario.id = ${funcionario.id}"
        String hql = "SELECT isEntrada FROM RegistroPonto WHERE id = (${queryFilter})"
        List isEntrada = RegistroPonto.executeQuery(hql)
        RegistroPonto registroPonto = new RegistroPonto()
        registroPonto.funcionario = funcionario
        registroPonto.dia = data
        registroPonto.hora = hora
        registroPonto.isEntrada = isEntrada.size() > 0 ? !isEntrada.get(0) : true
        return registroPonto.save() != null
    }

    void removerTodosPontosDoDia(LocalDate dia) {
        List<RegistroPonto> pontos = RegistroPonto.findAllByDia(dia)
        for (ponto in pontos) {
            ponto.delete()
        }
    }

    List<Dia> listaPontosMesCorrente(long idFuncionario) {

        List<RegistroPonto> pontos = (List<RegistroPonto>) RegistroPonto.withCriteria {
            eq('funcionario.id', idFuncionario)
            gt('dia', ConfiguracaoService.getDiaFechamento())
            order('dia', 'desc')
        }
        List<Dia> dias = []
        pontos.groupBy { it.dia.get(DateTimeFieldType.dayOfMonth()) }
                .each { pontosDoDia ->

            Dia objetoDia = new Dia()
            objetoDia.data = pontosDoDia.value.get(0).dia
            pontosDoDia.value.each { ponto ->
                objetoDia.pontos.add(ponto)
            }
            objetoDia.pontos.sort{it.hora}
            dias.add(objetoDia)
            dias = dias.sort { it.data }
        }
        return dias
    }
}
