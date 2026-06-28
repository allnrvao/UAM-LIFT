package ni.edu.uam.UAM_LIFT.dto;


public class EstadisticasUsuarioDTO {

    private int    totalViajes;
    private double kilometrosTotales;
    private double co2Ahorrado;

    public EstadisticasUsuarioDTO(int totalViajes, double kilometrosTotales, double co2Ahorrado) {
        this.totalViajes        = totalViajes;
        this.kilometrosTotales  = kilometrosTotales;
        this.co2Ahorrado        = co2Ahorrado;
    }

    public int    getTotalViajes()        { return totalViajes; }
    public double getKilometrosTotales()  { return kilometrosTotales; }
    public double getCo2Ahorrado()        { return co2Ahorrado; }
}