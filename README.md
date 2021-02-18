Objetivo
El objetivo de este ejercicio es crear un pequeño microservicio/aplicación que genere registros ficticios de posición de un equipo GPS virtual que simule moverse y los envíe a un web service receptor de SITRACK, cada 1 minuto un nuevo reporte con fecha/hora real de generación. No es relevante si el movimiento es aleatorio, en línea recta, en círculo, grandes o cortas distancias. Pueden ir valores aleatorios en los campos dentro de los rangos estipulados. Es suficiente con mantener activo el proceso del microservicio entre 5 y 10 minutos.
Implementación
El sistema externo debe enviar paquetes del protocolo HTTPS mediante el método PUT hacia el cluster de servidores receptores de Sitrack incluyendo en el contenido (body) del paquete un solo registro/objeto en formato JSON (http://json.org/) indicando la cabecera HTTP correspondiente: Content-Type: application/json
Cada invocación HTTP debe ser autenticada con una nueva firma digital generada dinámicamente por la aplicación generadora de reportes a partir de una clave secreta de aplicación que Sitrack entrega.
El método de generación de firma digital se especifica en la última sección Autenticación de reportes entrantes de este documento.
A continuación, la URL de ambiente de pruebas:
https://test-externalrgw.ar.sitrack.com/frame
Acuse de recibo
Por cada reporte recibido en la plataforma SITRACK se envía una respuesta con un código HTTP según el estándar: http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10. Cuando se recibió de manera correcta será el paquete de respuesta con código HTTP 200 OK ó un código de error HTTP en caso contrario, en cuyo contenido (body) del paquete puede observarse el detalle del error.
Antes de enviar un nuevo reporte de un equipo GPS determinado, el sistema productor de reportes debe esperar el acuse de recibo de SITRACK HTTP 200 OK del reporte inmediato anterior generado por el mismo equipo, lo cual implica que en cada instante solo puede haber una conexión HTTP correspondiente a un equipo en particular.
Un paquete/reporte para el cual se recibió un acuse de recibo HTTP 200 OK no debe volver a enviarse.
En caso de cualquier error de conexión (sin respuesta HTTP), o al recibir respuesta con código HTTP 429 Too Many Requests ó código HTTP igual o mayor a 500, el sistema productor de reportes deberá reintentar la transmisión del mismo reporte tantas veces como sea necesario luego de un tiempo de espera de al menos 10 segundos, hasta obtener el código de respuesta HTTP 200 OK.​ 
Estructura de reporte
Campos obligatorios
Todos los reportes deben contener los siguientes datos:
loginCode:string: identificador del vehículo o activo generado por Sitrack. Es informado luego de darse de alta que Sitrack, si se trata de un vehículo puede ser el dominio/placa/patente entregado por entidad estatal competente, normalizado sin espacios ni guiones. Este valor no deberá cambiar a pesar de que sea reemplazado el equipo/dispositivo GPS físico en el vehículo.
reportDate:string: fecha y hora de generación del reporte. El formato estándar de fecha para el intercambio de datos es ISO 8601 adoptado por el W3C: “yyyy-MM-ddTHH:mm:ss-ZZ:XX” (http://www.w3.org/TR/NOTE-datetime). Se recomienda utilizar huso horario UTC ​+​00​:00, el cual se puede representar abreviado con el caracter Z (zero), de la siguiente manera: ​“yyyy-MM-ddTHH:mm:ssZ”.
reportType:string: identificador de tipo de reporte/evento que se está enviando. Se utiliza “2” para indicar que es un Reporte por tiempo.
latitude:double: Latitud de la posición expresada en grados decimales. (0,00001 ° ~ 2 metros)
longitude:double: Longitud de la posición expresada en grados decimales. (0,00001 ° ~ 2 metros)
gpsDop:double: Dispersión de la precisión GPS. Los valores que representan coordenadas confiables son menores a 2. El valor 2 o más indica coordenadas poco precisas, por lo que los reportes pueden ser catalogados como inválidos y para poder observarlos es necesario activar filtros especiales.
(0.0 - 0.9) excelente
(1.0 - 1.9) buena
(2.0 - 9.9) escasa (no confiable)
(10.0 - 19.9) pobre
(20.0 - ∞) pésima
Referencia: https://en.wikipedia.org/wiki/Dilution_of_precision_(navigation)
Campos adicionales
Los siguientes campos adicionales deben enviarse siempre que sea posible, y cuando no estén disponibles debe omitirse la inclusión de su nombre en el registro JSON:
heading:integer: Rumbo de circulación expresado en grados sexagecimales relativos al Norte magnético ubicado en 0°. Valore válidos entre 0 y 359.
speed:double: Velocidad de movimientos expresada en Km/h.
speedLabel:double: Tipo de velocidad: GPS, ECU, ECU_MAX, ECU_AVG, etc
gpsSatellites:integer: Cantidad de satélites utilizados en cada cálculo de coordenadas.
text:string: texto asociado al reporte utilizado solamente como información adicional ajena a validaciones.
textLabel:string: Tipo de texto: TAG (uso general), VEHICLE_ID (VIN/chasis o id registro nacional)
Ejemplo de reporte
{"loginCode":"98173","reportDate":"2015-04-30T13:55:20Z","reportType":"2","latitude":-32.123456,"longitude":-68.123456,"gpsDop":1.0,"gpsSatellites":3,"heading":335,"speed":45.0,"speedLabel":"GPS","text":"Ficticio","textLabel":"TAG"}
Autenticación de reportes entrantes
La autenticación se realiza mediante la generación de una firma digital (signature) por cada paquete de transferencia de datos.
Por lo tanto, en cada invocación/request HTTP, en la cual deberá incluirse el encabezado estándar de HTTP Authorization indicando que el esquema de autenticación es SWSAuth (Sitrack Web Services Authentication Schema), seguido de los parámetros de autenticación según la sintaxis estándar de HTTP para el encabezado (separados por comas y con sus valores entre comillas), de la siguiente manera:
Authorization: SWSAuth application="ID",signature="HASH",timestamp="SECONDS"
En donde:
ID debe reemplazarse por el identificador de la entidad proveedora de reportes, el cual será entregado por Sitrack al momento de acordar la integración.
HASH debe reemplazarse por el resultado de la siguiente operación:
 Base64Enconder( MD5( application + secretKey + timestamp ) )
en donde:
Base64Encoder es la función de codificación en base 64 definida en el estándar IETF (http://tools.ietf.org/html/rfc3548#section-3)
MD5 es la función hash de resumen criptográfico de 128 bits definida en el estándar IETF (http:///tools.ietf.org/html/rfc1321). Debe tenerse en cuenta que el resultado de la función MD5 es un compendio de 128 bits (16 bytes) que son la entrada del proceso de codificación Base64, y no la codificación Hexadecimal de 32 caracteres. Por lo tanto la firma (signature) siempre resultará con una longitud de 24 caracteres. Una herramienta online donde puede verificarse la generación de un MD5 base64 hash es https://approsto.com/md5-generator
secretKey es la clave secreta que entrega Sitrack al dar de alta una nueva entidad proveedora de reportes.
El operador + es la función de concatenación de cadenas de caracteres.
SECONDS debe reemplazarse por la estampa de tiempo estándar de tipo unix del momento en que fue generada la Firma de la invocación. El estándar POSIX define un timestamp como segundos transcurridos desde las 0:00 hs UTC del 1/1/1970 (http://es.wikipedia.org/wiki/Tiempo_Unix).
Un ejemplo de la cabecera HTTP correcto es:
Authorization: SWSAuth application="MyTrackSystem", signature="lKD+lXU+iQjonvL1/c5hZw==",timestamp="1400599561"
La Firma, signature, siempre resultará con una longitud de 24 caracteres, y el timestamp consta de 10 dígitos.
Si la autenticación es correcta se responderá un paquete con el código estándar definido por el protocolo HTTP 200 OK
La característica de seguridad del método de autenticación consiste en que nunca se transmite sobre internet la Clave Secreta desde la aplicación remota del cliente hacia el servidor, sino que solo se transmite la Firma que cambia en todas las invocaciones y a partir de la cual es imposible en la práctica generar la Clave Secreta.
Importante
No son aceptadas firmas generadas con un timestamp anterior al último timestamp de una invocación correcta. Cada firma solo es aceptada durante un período de tiempo de 10 minutos posterior al instante indicado por el timestamp de autenticación, por lo tanto también implica que la aplicación cliente que consuma el servicio web debe tener ajustada la fecha y hora con un margen de error menor a 10 minutos de atraso o adelantamiento con respecto a la hora universal. 
Si no se cumple alguna de las condiciones de seguridad indicadas, el servicio web responderá un paquete con el código de error estándar HTTP 401 Unauhorized, y en el contenido se proporcionarán los campos “errorCode” y “userMessage” con la especificación del motivo de denegación. El campo “detailedMessage” se entrega opcionalmente para usuarios desarrolladores, cuando existe algún detalle adicional.
De manera predeterminada el contenido de la respuesta de error es en formato JSON.
{"userMessage":"Acceso denegado. Falta un parámetro de autenticación.",
"errorCode":114,
"detailedMessage":"Missing parameter",
"responseCode":401
}

{"userMessage":"Acceso denegado. El id de aplicación o firma de autenticación es inválida."
"errorCode":115,
"detailedMessage":"Invalid application id or signature",
"responseCode":401
}

{"userMessage":"Acceso denegado. El timestamp de autenticación es inválido.",
"errorCode":116,
"detailedMessage":"Timestamp is 29 seconds earlier than last valid received timestamp.",
"responseCode":401
}

{"userMessage":"Acceso denegado. El timestamp de autenticación es inválido.",
"errorCode":116,
"detailedMessage":"Exceeded timestamp difference: 602 seconds to UTC 00",
"responseCode":401
}
