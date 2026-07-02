package ni.edu.uam.uamlift.data.enums

data class Departamento(
    val nombre: String,
    val lat: Double,
    val lng: Double
)

object DepartamentosPacifico {
    // Los 16 departamentos REALES DE NICARAGUA
    private val departamentos = listOf(
        Departamento("Chinandega", 12.6289, -87.1370),
        Departamento("León", 12.4371, -86.8877),
        Departamento("Managua", 12.1147, -86.2362),
        Departamento("Masaya", 11.9736, -86.0913),
        Departamento("Granada", 11.9294, -85.9547),
        Departamento("Carazo", 11.6944, -86.4917),
        Departamento("Rivas", 11.4412, -85.3444),
        Departamento("Estelí", 13.0833, -86.3667),
        Departamento("Madriz", 13.3167, -86.5000),
        Departamento("Nueva Segovia", 14.0833, -86.2667),
        Departamento("Jinotega", 13.3167, -85.8333),
        Departamento("Matagalpa", 12.9167, -85.9167),
        Departamento("Boaco", 12.4667, -85.7667),
        Departamento("Chontales", 12.4167, -85.2500),
        Departamento("Río San Juan", 10.9833, -84.4167),
        Departamento("Regiones Autónomas Atlánticas", 12.5, -84.0)
    )
    
    fun getAll(): List<Departamento> = departamentos
    fun getByName(nombre: String): Departamento? = departamentos.find { it.nombre == nombre }
}
