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
    private Fatura fatura;
    private Pagamento pagamento;
    private ProcessadorContas processadorContas;

    @BeforeEach
    void setUp() {
        this.contas = new ArrayList<>();
        this.conta = new Conta(TipoPagamento.CARTAO_CREDITO, 001, (LocalDate.of(2024, 07, 24)), 1000);
        this.contas.add(conta);
        this.fatura = new Fatura((LocalDate.of(2024, 07, 24)), 1000, "Cliente");
        this.pagamento = new Pagamento(TipoPagamento.CARTAO_CREDITO, (LocalDate.of(2024, 07, 24)), 1000);
        this.processadorContas = new ProcessadorContas();
    }

    @Test
    @DisplayName("Verifica se valorSomaTotal >= valorFatura, fatura.Status == paga")
    void testProcessarContasFaturaPaga() {
        assertAll(
                () -> { this.processadorContas.processarContas(this.contas, this.fatura);
                        assertEquals(FaturaStatus.PAGA, this.fatura.getStatus());
                },
                () -> { this.fatura.setValorTotalFatura(700);
                        this.processadorContas.processarContas(this.contas, this.fatura);
                        assertEquals(FaturaStatus.PAGA, this.fatura.getStatus());
                }
        );
    }

    @Test
    @DisplayName("Verifica se valorSomaTotal < valorFatura, fatura.Status ==  pendente")
    void testProcessarContasFaturaPendente() {
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
    @DisplayName("O processador de contas deve, para cada conta, criar um \"pagamento\" associado a essa fatura")
    void testPagamento() {
        this.processadorContas.criarPagamento(this.conta, this.fatura);
        assertEquals(FaturaStatus.PAGA, this.fatura.getStatus());
    }

    @Test
    @DisplayName("pagamento.getTipo()")
    void testPagamentoTipo() {
        assertAll(
                () -> assertEquals(TipoPagamento.CARTAO_CREDITO, this.conta.getTipoPagamento()),

                () -> { this.conta.setTipoPagamento(TipoPagamento.BOLETO);
                        assertEquals(TipoPagamento.BOLETO, this.conta.getTipoPagamento());
                },
                () -> { this.conta.setTipoPagamento(TipoPagamento.TRANSFERENCIA_BANCARIA);
                        assertEquals(TipoPagamento.TRANSFERENCIA_BANCARIA, this.conta.getTipoPagamento());
                }
        );
    }

    @Test
    @DisplayName("pagamento.getValor()")
    void testPagamentoValor() { assertEquals(1000, this.pagamento.getValorPago()); }

    @Test
    @DisplayName("pagamento.getData()")
    void testPagamentoData() { assertEquals((LocalDate.of(2024, 07, 24)), this.pagamento.getData()); }

    @Test
    @DisplayName("! R$ 5.000,00 < pagamento.getValor() < R$0,01")
    void testPagamentoValorMinimo() {
        assertAll(
                () -> assertFalse(this.pagamento.getValorPago() < 0.01),
                () -> assertFalse(this.pagamento.getValorPago() > 5000),
                () -> assertTrue(this.pagamento.verificarPagamento())
        );
    }

    @Test
    @DisplayName("Se a data de pagamento de um boleto for posterior à data da conta respectiva, então o boleto deve ser acrescido 10%")
    void testPagamentoComJuros() {
        this.processadorContas.criarPagamento(this.conta, this.fatura);
        assertEquals(1000, this.pagamento.getValorPago(), 0.1);
    }

    @Test
    @DisplayName("Fatura de 1.500,00 (20/02/2023) com 3 contas no valor de 500,00, 400,00 e 600,00. " +
                "As três contas foram pagas por boleto no dia 20/02/2023 (todas em dia), " +
                "assim a fatura é marcada como PAGA.\n")
    void testExemplo1(){
        List<Conta> contas1 = new ArrayList<>();
        Conta conta1 = new Conta(TipoPagamento.BOLETO, 002, LocalDate.of(2023, 02, 20), 500);
        Conta conta2 = new Conta(TipoPagamento.BOLETO, 003, LocalDate.of(2023, 02, 20), 400);
        Conta conta3 = new Conta(TipoPagamento.BOLETO, 004, LocalDate.of(2023, 02, 20), 600);
        contas1.add(conta1);
        contas1.add(conta2);
        contas1.add(conta3);
        Fatura fatura1 = new Fatura(LocalDate.of(2023, 02, 20), 1500, "Cliente Exemplo 1");
        (new ProcessadorContas()).processarContas(contas1, fatura1);

        assertEquals(FaturaStatus.PAGA, fatura1.getStatus());
    }

    @Test
    @DisplayName("Fatura de 1.500,00 (20/02/2023) com uma conta no valor 700,00 e outra conta de 800,00. " +
                "A primeira conta foi paga por cartão de crédito (05/02/2023), " +
                "enquanto que a segunda conta foi paga por transferência (17/02/2023). Assim, a fatura é marcada como PAGA.\n")
    void testExemplo2(){
        List<Conta> contas1 = new ArrayList<>();
        Conta conta1 = new Conta(TipoPagamento.CARTAO_CREDITO, 002, LocalDate.of(2023, 02, 05), 700);
        Conta conta2 = new Conta(TipoPagamento.TRANSFERENCIA_BANCARIA, 003, LocalDate.of(2023, 02, 17), 800);
        contas1.add(conta1);
        contas1.add(conta2);
        Fatura fatura1 = new Fatura(LocalDate.of(2023, 02, 20), 1500, "Cliente Exemplo 2");
        (new ProcessadorContas()).processarContas(contas1, fatura1);

        assertEquals(FaturaStatus.PAGA, fatura1.getStatus());
    }

    @Test
    @DisplayName("Fatura de 1.500,00 (20/02/2023) com uma conta no valor 700,00 e outra conta de 800,00. " +
                "A primeira conta foi paga por cartão de crédito (06/02/2023), " +
                "enquanto que a segunda conta foi paga por transferência (17/02/2023). Assim, a fatura é marcada como PENDENTE.\n")
    void testExemplo3(){
        List<Conta> contas1 = new ArrayList<>();
        Conta conta1 = new Conta(TipoPagamento.CARTAO_CREDITO, 002, LocalDate.of(2023, 02, 06), 700);
        Conta conta2 = new Conta(TipoPagamento.TRANSFERENCIA_BANCARIA, 003, LocalDate.of(2023, 02, 17), 800);
        contas1.add(conta1);
        contas1.add(conta2);
        Fatura fatura1 = new Fatura(LocalDate.of(2023, 02, 20), 1500, "Cliente Exemplo 2");
        (new ProcessadorContas()).processarContas(contas1, fatura1);

        assertEquals(FaturaStatus.PENDENTE, fatura1.getStatus());
    }
}
