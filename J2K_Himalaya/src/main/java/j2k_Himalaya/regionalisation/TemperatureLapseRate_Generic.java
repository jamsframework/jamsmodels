package j2k_Himalaya.regionalisation;

import jams.data.Attribute;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;

@JAMSComponentDescription(
        title = "TemperatureLapseRate",
        author = "Santosh Nepal, Peter Krause",
        description = "Regionalisation of Temp through general adiabatic "
        + "ratedepends upon given adaiabatic rate +++ included "
        + "seasonal lapse rate. Two different Lapse rate for "
        + "Summer and Winter season is proposed")
public class TemperatureLapseRate_Generic extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "station elevation")
    public Attribute.DoubleArray statElev;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "entity elevation")
    public Attribute.Double entityElev;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the measured input from a base station")
    public Attribute.DoubleArray inputValue;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "calculated output for the modelling entity")
    public Attribute.Double outputValue;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "lapse rates for each month of the year and elevation difference")
    public Attribute.Double[] lapseRates;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current model time")
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "position array to determine best weights")
    public Attribute.IntegerArray statOrder;

    public void run() {
        int closestStation = statOrder.getValue()[0];

        double elevationdiff = statElev.getValue()[closestStation] - entityElev.getValue();

        int nowmonth = time.get(2);

        outputValue.setValue(elevationdiff * (lapseRates[nowmonth].getValue() / 100.0D) + inputValue.getValue()[closestStation]);
    }

}
