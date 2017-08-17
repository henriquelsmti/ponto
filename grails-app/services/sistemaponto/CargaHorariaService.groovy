package sistemaponto

import grails.transaction.Transactional
import org.springframework.transaction.TransactionStatus

@Transactional
class CargaHorariaService {

    def salvar(CargaHoraria cargaHoraria) {
        if(cargaHoraria.save()){
            return true
        }
        return false
    }

    def getAll(){
        return CargaHoraria.list(sort: 'id')
    }


    boolean excluir(CargaHoraria cargaHoraria){
        CargaHoraria.withTransaction{ TransactionStatus status ->
                cargaHoraria.delete()
                return true
        }
        return false
    }

    def getOne(long id){
        println "ID:::{$id}"
        return CargaHoraria.findById(id)
    }
}