package vev.processador_contas;

import org.junit.jupiter.api.*;
import jakarta.transaction.Transactional;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.*;

@SpringBootTest
@Transactional
class ProcessadorContasApplicationTests {

    private List<Conta> contas;
    private Conta conta;
    private List<Fatura> faturas;
    private Fatura fatura;
    private Pagamento pagamento;
    private ProcessadorContas processadorContas;

    @BeforeEach
    void setUp() {
        this.contas = new ArrayList<>();
        this.conta = new Conta(001, (LocalDate.of(2024, 07, 24)), 1000);
        this.contas.add(conta);
        this.faturas = new ArrayList<>();
        this.fatura = new Fatura((LocalDate.of(2024, 07, 24)), 1000, "Cliente");
        this.faturas.add(fatura);
        this.pagamento = new Pagamento();
        this.processadorContas = new ProcessadorContas(this.contas, this.fatura);
    }

    @Test
    @DisplayName("Verifica se valorSomaTotal >= valorFatura, fatura.Status == paga")
    void testVerificaSeValorSomaTotalFatura() {
        this.processadorContas.processarContas(this.contas, this.fatura);
        assertEquals(FaturaStatus.PAGA, this.fatura.getStatus());
        this.fatura.setValorTotalFatura(700);
        this.processadorContas.processarContas(this.contas, this.fatura);
        assertEquals(FaturaStatus.PAGA, this.fatura.getStatus());
    }

    @Test
    @DisplayName("Verifica se valorSomaTotal < valorFatura, fatura.Status ==  pendente")
    void testVerificaSeValorSomaTotalPendente() {
        this.conta.setValorPagoConta(700);
        this.processadorContas.processarContas(this.contas, this.fatura);
        assertEquals(FaturaStatus.PENDENTE, this.fatura.getStatus());
    }

    @Test
    @DisplayName("fatura.getData()")
    void testFaturaData() { assertEquals((LocalDate.of(2024, 07, 24)), this.fatura.getData()); }

    @Test
    @DisplayName("fatura.getValorTotalFatura()")
    void testFaturaValorTotalFatura() { assertEquals(1000, this.fatura.getValorTotalFatura()); }

    @Test
    @DisplayName("fatura.getNomeCliente()")
    void testFaturaNomeCliente() { assertEquals("Cliente", this.fatura.getNomeCliente()); }

    @Test
    @DisplayName("conta.getCodigoConta()")
    void testContaCodigo() { assertEquals(001, this.conta.getCodigoConta()); }

    @Test
    @DisplayName("conta.getData()")
    void testContaData() { assertEquals((LocalDate.of(2024, 07, 24)), this.conta.getData()); }

    @Test
    @DisplayName("conta.getValorPago()")
    void testContaValorPago() { assertEquals(1000, this.conta.getValorPagoConta()); }

    @Test
    @DisplayName("processadorContas.getListaContas()")
    void testListaContas() { this.processadorContas.getContas(); }

    @Test
    @DisplayName("O processador de contas deve, para cada conta, criar um \"pagamento\" associado a essa fatura")
    void testPagamento() {
        this.processadorContas.criarPagamento();
        assertEquals(FaturaStatus.PAGA, this.fatura.getStatus());
    }

    @Test
    @DisplayName("pagamento.getTipo()")
    void testPagamentoTipo() {
        assertEquals(TipoPagamento.BOLETO, this.conta.getPagamento());
        this.conta.setPagamento(TipoPagamento.CARTAO_CREDITO);
        assertEquals(TipoPagamento.CARTAO_CREDITO, this.conta.getPagamento());
        this.conta.setPagamento(TipoPagamento.CARTAO_CREDITO);
        assertEquals(TipoPagamento.TRANSFERENCIA_BANCARIA, this.conta.getPagamento());
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
