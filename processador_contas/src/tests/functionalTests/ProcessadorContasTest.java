package tests.functionalTests;

import static org.junit.Assert.assertEquals;

// import java.time.LocalDate;
// import java.util.ArrayList;
// import java.util.List;
import org.junit.jupiter.api.*;
// import static org.junit.jupiter.api.Assertions.*;

// import main.models.*;
// import main.enumerations.*;
// import main.service.ProcessadorContas;

public class ProcessadorContasTest {

    // private List<Conta> contas;
    // private Conta conta;
    // private Fatura fatura;
    // private Pagamento pagamento;
    // private ProcessadorContas processadorContas;

    @BeforeEach
    public void setUp() {
        // this.contas = new ArrayList<>();
        // this.conta = new Conta(TipoPagamento.CARTAO_CREDITO, 001, (LocalDate.of(2024, 07, 24)), 1000);
        // this.contas.add(conta);
        // this.fatura = new Fatura((LocalDate.of(2024, 07, 24)), 1000, "Cliente");
        // this.pagamento = new Pagamento(TipoPagamento.CARTAO_CREDITO, (LocalDate.of(2024, 07, 24)), 1000);
        // this.processadorContas = new ProcessadorContas();
    }

    @Nested
    @DisplayName("Executando testes de 'Análise do Valor Limite'")
    class AVLTest {

        @Test
        @DisplayName("")
        public void test() { assertEquals(1,1); }
    }

    @Nested
    @DisplayName("Executando testes de 'Partições de Equivalência'")
    class ParticaoTest {

        @Test
        @DisplayName("")
        public void test() { assertEquals(1,1); }
    }

    @Nested
    @DisplayName("Executando testes de 'Tabelas de Decisão'")
    class TabelaTest {

        @Test
        @DisplayName("")
        public void test() { assertEquals(1,1); }
    }
}
