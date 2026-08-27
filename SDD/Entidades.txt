Las Entidades del Modelo de Dominio de NexusMarket representan los objetos principales del negocio que poseen una identidad propia y que pueden cambiar de estado a lo largo del tiempo. Estas entidades permiten representar de manera estructurada los diferentes elementos que intervienen en el funcionamiento de la plataforma Marketplace.

Para su construcción se aplican conceptos de Programación Orientada a Objetos y principios de Diseño Dirigido por el Dominio, permitiendo identificar las entidades que contienen información y comportamientos relacionados con las reglas del negocio.

El modelo contempla principalmente las siguientes entidades:

•	Usuario, representa a una persona registrada en la plataforma y contiene la información necesaria para su identificación y acceso al sistema.
•	Comprador, representa al usuario que adquiere productos a través de NexusMarket y participa en el proceso de compra.
•	Vendedor, representa a la persona o negocio encargado de ofrecer y comercializar productos dentro de la plataforma.
•	Producto, representa los artículos físicos o digitales disponibles para la venta, incluyendo información como nombre, descripción, precio y disponibilidad.
•	Bodega, representa el espacio destinado al almacenamiento y administración de los productos.
•	Inventario, permite controlar las existencias de los productos y su disponibilidad dentro de las bodegas.
•	Carrito, representa la selección temporal de productos que realiza un comprador antes de confirmar una compra.
•	Pedido, representa una compra realizada por un comprador y contiene los productos, cantidades, valores y estado de la compra.
•	Factura, representa el documento asociado a una compra, registrando la información económica de la transacción.
•	Envío, representa el proceso de entrega de un pedido al comprador, incluyendo la información necesaria para su seguimiento y estado.
•	Devolución, representa la solicitud realizada por un comprador para devolver uno o varios productos adquiridos.
•	Reembolso, representa el proceso mediante el cual se devuelve al comprador el dinero correspondiente a una compra o devolución aprobada.

De esta manera, las Entities de NexusMarket permiten representar los principales objetos del negocio y sus relaciones, facilitando posteriormente su implementación mediante clases en el código. Cada entidad mantiene una identidad propia y puede contener atributos, comportamientos y reglas de negocio, convirtiéndose en una base fundamental para el desarrollo de la aplicación.

Entities
├── Usuario
├── Comprador
├── Vendedor
├── Producto
├── Bodega
├── Inventario
├── Carrito
├── Pedido
├── Factura
├── Envio
├── Devolucion
└── Reembolso