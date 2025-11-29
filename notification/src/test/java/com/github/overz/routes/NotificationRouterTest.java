package com.github.overz.routes;

import com.github.overz.models.Notification;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste unitário para a rota REST "restOrderEntrypoint" (direct:rest-order-entrypoint)
 * que busca dados no banco através da rota interna e retorna uma informação em JSON.
 *
 * O acesso ao banco é simulado substituindo a chamada para "direct:find-order-notification"
 * por um processador que devolve uma lista com um Notification.
 */
public class NotificationRouterTest {

    private CamelContext context;
    private ProducerTemplate template;

    @BeforeEach
    void setUp() throws Exception {
        context = new DefaultCamelContext();
        context.addRoutes(new NotificationRouter());

        // Substitui a chamada ao endpoint interno por uma simulação
        AdviceWith.adviceWith(context, "app-route.redirect-post-order", builder -> {
            // Substitui a origem (rest://...) por um endpoint direto para facilitar o teste
            builder.replaceFromWith("direct:rest-order-entrypoint");
            builder.weaveByToUri("direct:find-order-notification").replace().process(exchange -> {
                // Gera uma resposta simulada, utilizando o id definido como propriedade na rota
                final String id = exchange.getProperty(Notification.Fields.cdOrder, String.class);
                List<Notification> result = new ArrayList<>();
                result.add(Notification.builder()
                    .cdOrder(id)
                    .cdNotification("n1")
                    .build());
                exchange.getMessage().setBody(result);
            });
            // Remove a etapa de marshal JSON para manter a lista como body no teste
            builder.weaveByType(org.apache.camel.model.MarshalDefinition.class).remove();
        });

        // Desabilita a rota SOAP para não precisar do bean CXF
        AdviceWith.adviceWith(context, "app-route.notification-soap-get-order", builder ->
            builder.replaceFromWith("direct:disabled-soap")
        );

        // Desabilita a rota que consulta SQL diretamente para evitar resolver o endpoint SQL
        AdviceWith.adviceWith(context, "app-route.find-order-notification", builder -> {
            builder.replaceFromWith("direct:disabled-find");
            builder.weaveByToUri("sql:*").replace().to("mock:sql");
        });

        // Evita resolver propriedades SMTP substituindo pelo mock
        AdviceWith.adviceWith(context, "app-route.send-email", builder ->
            builder.weaveByToUri("smtp://*").replace().to("mock:smtp")
        );

        // Evita resolver o endpoint SQL no salvamento
        AdviceWith.adviceWith(context, "app-route.save-confirmation", builder ->
            builder.weaveByToUri("sql:*").replace().to("mock:sql")
        );

        // Desabilita o consumidor Kafka, evitando resolver o endpoint kafka
        AdviceWith.adviceWith(context, "app-route.notification-consumer", builder ->
            builder.replaceFromWith("direct:disabled-kafka")
        );

        context.start();
        template = context.createProducerTemplate();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (template != null) {
            template.stop();
        }
        if (context != null) {
            context.stop();
        }
    }

    @Test
    void shouldReturnNotificationListFromRestOrderEntrypoint() {
        // Envia requisição para a rota testada com o header "id"
        final var out = template.request("direct:rest-order-entrypoint", exchange ->
            exchange.getMessage().setHeader("id", "123")
        );

        // Verifica que a rota retornou uma lista com a notificação simulada
        @SuppressWarnings("unchecked")
        final List<Notification> list = out.getMessage().getBody(List.class);
        assertNotNull(list, "O corpo da resposta (lista) não deveria ser nulo");
        assertEquals(1, list.size(), "Deve retornar exatamente um item");
        assertEquals("123", list.get(0).getCdOrder(), "O id do pedido deve ser propagado para a busca");
        assertEquals("n1", list.get(0).getCdNotification(), "Código de notificação simulado deve estar presente");

        // A rota remove todos os headers antes de serializar
        assertNull(out.getMessage().getHeader("id"), "Header 'id' deve ser removido pela rota");
    }
}
