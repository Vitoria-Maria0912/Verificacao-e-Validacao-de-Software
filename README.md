# Atividades desenvolvidas para a disciplina de Verificação e Validação de Software

## Processador de Contas
    Deve-se implementar um processador de contas. O objetivo desse processador é verificar todas as contas e, caso o valor da soma de todas as contas seja maior ou igual ao valor da fatura, então essa fatura deverá ser considerada como paga, caso contrário será pendente.

    Uma fatura contém data, valor total e nome do cliente. Uma conta contém o código da conta, data, e valor pago.

    O processador de contas, ao receber uma lista de contas, deve então, para cada conta, criar um "pagamento" associado a essa fatura. Esse pagamento contém o valor pago, a data, e o tipo do pagamento efetuado. Há três tipos de pagamento: boleto ("BOLETO"), cartão de crédito ("CARTAO_CREDITO") e transferência bancária ("TRANSFERENCIA_BANCARIA"). Pagamentos por boleto não podem ter valor inferior a R$ 0,01 nem superior a R$ 5.000,00. Além disso, se a data de pagamento de um boleto for posterior à data da conta respectiva, então o boleto foi pago com atraso e deve ser acrescido 10% no valor do pagamento.

    Como dito anteriormente, caso a soma de todos os pagamentos seja igual ou ultrapasse o valor da fatura, a mesma deve ser marcada como "PAGA", caso contrário será marcada como “PENDENTE”. Pagamentos do tipo cartão de crédito só podem ser incluídos na soma caso a data da conta seja de pelo menos 15 dias anteriores à data da fatura. Os outros tipos devem ser incluídos desde que a data da conta seja igual ou anterior à data da fatura. Em ambos os casos considerar apenas data, mês e ano.

    Exemplo 1: Fatura de 1.500,00 (20/02/2023) com 3 contas no valor de 500,00, 400,00 e 600,00. As três contas foram pagas por boleto no dia 20/02/2023 (todas em dia), assim a fatura é marcada como PAGA.

    Exemplo 2: Fatura de 1.500,00 (20/02/2023) com uma conta no valor 700,00 e outra conta de 800,00. A primeira conta foi paga por cartão de crédito (05/02/2023), enquanto que a segunda conta foi paga por transferência (17/02/2023). Assim, a fatura é marcada como PAGA.

    Exemplo 3: Fatura de 1.500,00 (20/02/2023) com uma conta no valor 700,00 e outra conta de 800,00. A primeira conta foi paga por cartão de crédito (06/02/2023), enquanto que a segunda conta foi paga por transferência (17/02/2023). Assim, a fatura é marcada como PENDENTE.

## Sistema de Ingressos
    Deve-se implementar um sistema de venda de ingressos para shows.

    Um show tem data, artista, cachê, total de despesas de infraestrutura, lotes de ingressos e se o show será realizado em data especial ou não. De 20 a 30% dos ingressos devem ser do tipo VIP, 10% dos ingressos devem ser reservados para MEIA_ENTRADA, enquanto o restante é NORMAL. Um ingresso contém id, tipo e status. O status pode ser vendido ou não. Um ingresso VIP deve custar o dobro de um ingresso NORMAL e um ingresso MEIA_ENTRADA deve custar a metade do NORMAL.

    Deve ser possível marcar um ingresso como vendido ou não.

    Ingressos são vendidos em lotes, cada lote tem um id, um conjunto de ingressos e um desconto aplicável. Assim, um lote pode oferecer ou não um desconto em ingressos, sendo o desconto máximo permitido de 25%. O desconto só é válido para ingressos VIP e NORMAL.

    Deve ser possível gerar um relatório do show. O relatório contém o número de ingressos vendidos de cada tipo, a receita líquida e o status financeiro do show. A receita líquida do show é calculada como sendo a soma dos valores de todos os ingressos vendidos, considerando os descontos aplicáveis, deduzidas as despesas de infraestrutura e o cachê do artista. Se o show for realizado em data especial, as despesas de infraestrutura têm um valor adicional de 15%. Por fim, o status financeiro é LUCRO quando a receita líquida é positiva, ESTÁVEL se for zero, e PREJUÍZO se a receita for negativa.

    Exemplo: um show em data especial com um artista com cachê de R$ 1.000,00, custos de infraestrutura de R$ 2.000,00 e um lote de ingressos à venda com 20% dos ingressos sendo VIP, e o preço do ingresso NORMAL é R$ 10,00. O lote tem 500 ingressos à venda com 15% de desconto, dos quais todos foram vendidos. Assim, a receita dos ingressos corresponde a R$ 4.925,00. Os custos correspondem a R$ 3.300,00. Portanto, o status financeiro do show é LUCRO, com lucro de R$ 1.625,00.

### Padrões de commit
- `feat`: adicionar funcionalidade
- `wip`: funcionalidade em desenvolvimento
- `fix`: corrigir bug
- `refactor`: refatorar código
- `test`: adicionar teste
- `docs`: adicionar documentação
- `perf`: melhorar desempenho
- `style`: melhorar estilo de código
- `build`: alterações na estrutura do projeto
- `chore`: alterações em pacotes, dependências
- `ci`: alterações de integração contínua