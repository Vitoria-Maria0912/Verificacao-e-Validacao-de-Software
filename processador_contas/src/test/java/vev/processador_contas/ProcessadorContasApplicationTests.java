package vev.processador_contas;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.*;

@SpringBootTest
@Transactional
class ProcessadorContasApplicationTests {

    private List<Conta> contas;
    private Conta conta;
    private Fatura fatura;
    private ProcessadorContas processadorContas;

    @BeforeEach
    void setUp() {
        this.contas = new ArrayList<>();
        this.conta = new Conta((LocalDate.of(2024, 07, 24)), 1000);
        this.fatura = new Fatura((LocalDate.of(2024, 07, 24)), 1000, "Cliente");
        this.processadorContas = new ProcessadorContas();
    }

    @AfterEach
    void tearDown() {}

    @Test
    @DisplayName("Verifica se valorSomaTotal >= valorFatura, fatura.Status == paga")
    void testVerificaSeValorSomaTotalFatura() {
        this.processadorContas.processarContas(this.fatura, this.contas);
        assert(this.fatura.getStatus() == FaturaStatus.PAGA);
    }

    @Test
    @DisplayName("Verifica se valorSomaTotal < valorFatura, fatura.Status ==  pendente")
    void testVerificaSeValorSomaTotalPendente() {
        this.processadorContas.processarContas(this.fatura, this.contas);
        assert(this.fatura.getStatus() == FaturaStatus.PENDENTE);
    }

    @Test
    @DisplayName("fatura.getData()")
    void testFaturaData() {
        assert(this.fatura.getData() == (LocalDate.of(2024, 07, 24)));
    }

    @Test
    @DisplayName("fatura.getValorTotal()")
    void testFaturaValorTotal() {
        assert(this.fatura.getValorTotal() == 1000);
    }

    @Test
    @DisplayName("fatura.getNomeCliente()")
    void testFaturaNomeCliente() {
        assert(this.fatura.getNomeCliente() == "Cliente");
    }

    @Test
    @DisplayName("conta.getCodigoConta()")
    void testContaCodigo() {}

    @Test
    @DisplayName("conta.getData()")
    void testContaData() {
        assert(this.conta.getData() == (LocalDate.of(2024, 07, 24)));
    }

    @Test
    @DisplayName("conta.getValorPago()")
    void testContaValorPago() {}

    @Test
    @DisplayName("processadorContas.getListaContas()")
    void testListaContas() {}

    @Test
    @DisplayName("pagamento.tipo.BOLETO | pagamento.tipo.CARTAO_CREDITO | pagamento.tipo.TRANSFERENCIA_BANCARIA")
    void testPagamentoTipo() {
    }

    @Test
    @DisplayName("pagamento.getValor()")
    void testPagamentoValor() {}

    @Test
    @DisplayName("pagamento.getData()")
    void testPagamentoData() {
    }

    @Test
    @DisplayName("! R$ 5.000,00 < pagamento.getValor() < R$0,01")
    void testPagamentoValorMinimo() {}

    @Test
    @DisplayName("Se a data de pagamento de um boleto for posterior à data da conta respectiva, então o boleto deve ser acrescido 10%")
    void testPagamentoComJuros() {}

    @Test
    @DisplayName("Fatura de 1.500,00 (20/02/2023) com 3 contas no valor de 500,00, 400,00 e 600,00. " +
                "As três contas foram pagas por boleto no dia 20/02/2023 (todas em dia), " +
                "assim a fatura é marcada como PAGA.\n")
    void testExemplo1(){
    }

    @Test
    @DisplayName("Fatura de 1.500,00 (20/02/2023) com uma conta no valor 700,00 e outra conta de 800,00. " +
                "A primeira conta foi paga por cartão de crédito (05/02/2023), " +
                "enquanto que a segunda conta foi paga por transferência (17/02/2023). Assim, a fatura é marcada como PAGA.\n")
    void testExemplo2(){}

    @Test
    @DisplayName("Fatura de 1.500,00 (20/02/2023) com uma conta no valor 700,00 e outra conta de 800,00. " +
                "A primeira conta foi paga por cartão de crédito (06/02/2023), " +
                "enquanto que a segunda conta foi paga por transferência (17/02/2023). Assim, a fatura é marcada como PENDENTE.\n")
    void testExemplo3(){}
}
