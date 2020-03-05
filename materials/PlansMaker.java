import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.util.Random;

public class PlansMaker {
    private static final Logger log = Logger.getLogger(PlansMaker1.class);
    private Scenario scenario;
    private PopulationFactory pf;
    private Random r = new Random(15);
    private Coord i15nb;
    private Coord i15sb;
    private CoordinateTransformation ct;

    public PlansMaker(String crs){
        scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        pf = scenario.getPopulation().getFactory();
        ct = TransformationFactory.getCoordinateTransformation(
                TransformationFactory.WGS84, crs);

        i15nb = ct.transform(CoordUtils.createCoord(-111.721902, 40.066630));
        i15sb = ct.transform(CoordUtils.createCoord(-111.738536, 40.049808));
    }



    public void makePlans(Integer numberofPeople){
        // make a plan for each person
        for(int i = 0; i < numberofPeople; i++){
            Person person = pf.createPerson(Id.createPersonId(i));
            Plan plan = pf.createPlan();

            // Get the plan figured out
            person.getAttributes().putAttribute("employed", true); // make random


            // Figure out the daily activity pattern
            // "W", "N", "H"
            String dap = "W"; // make this random, different distribution based on employment

            Double lat = 40.0444;
            Double lon = -111.7322;
            Coord homeCoord = ct.transform(CoordUtils.createCoord(lon, lat)); // random
            Activity homeActivity = pf.createActivityFromCoord("home", homeCoord);

            if(dap.equals("W")){
                // leave home around 7AM
                homeActivity.setEndTime(7 * 3600 + r.nextGaussian() * 30); // random!
                plan.addActivity(homeActivity);

                plan.addLeg(pf.createLeg("car"));

                // need to create a work activity
                Coord workCoord = ct.transform(i15nb);
                Activity workActivity = pf.createActivityFromCoord("work", workCoord);
                workActivity.setEndTime(17 * 3600 + r.nextGaussian() * 30);
                plan.addActivity(workActivity);

                plan.addLeg(pf.createLeg("car"));

                // need to create an optional other activity
                Coord otherCoord = ct.transform(i15nb); // make this random in payson
                Activity otherActivity = pf.createActivityFromCoord("other", otherCoord);
                otherActivity.setEndTime(18 * 3600 + r.nextGaussian() * 4); //
                plan.addActivity(otherActivity);

                plan.addLeg(pf.createLeg("car"));

                Activity homeAgain = pf.createActivityFromCoord("home", homeCoord);
                plan.addActivity(homeAgain);

            } else if(dap.equals("N")) {
                // need to create between 1 and 3 discretionary activities
            } else if(dap.equals("H")) {
                // No activities
            }



            person.addPlan(plan);
            scenario.getPopulation().addPerson(person);
        }
    }

    public void writePlans(String file){
        PopulationWriter writer = new PopulationWriter(scenario.getPopulation());
        writer.write(file);
    }


    public static void main(String[] args){
        PlansMaker1 pm = new PlansMaker1("EPSG:2849");
        pm.makePlans(10);
        pm.writePlans("output/plans.xml.gz");
    }
}

