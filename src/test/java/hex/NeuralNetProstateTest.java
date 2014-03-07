package hex;

import hex.nn.NN;
import hex.nn.NNModel;
import org.junit.BeforeClass;
import org.junit.Test;
import water.JUnitRunnerDebug;
import water.Key;
import water.TestUtil;
import water.UKV;
import water.fvec.Frame;
import water.fvec.NFSFileVec;
import water.fvec.ParseDataset2;
import water.util.Log;

public class NeuralNetProstateTest extends TestUtil {
  @BeforeClass public static void stall() {
    stall_till_cloudsize(JUnitRunnerDebug.NODES);
  }

  @Test public void run() {
    Key file = NFSFileVec.make(find_test_file("smalldata/./logreg/prostate.csv"));
    Frame frame = ParseDataset2.parse(Key.make(), new Key[]{file});

    Key dest = Key.make("prostate");

    // build the model, with all kinds of shuffling/rebalancing/sampling
    {
      NN p = new NN();
      p.epochs = 1000;
      p.source = frame;
      p.response = frame.vecs()[1];
      p.validation = frame;
      p.destination_key = dest;

      // Leaky
//      p.force_load_balance = true;
//      p.balance_classes = true;
//      p.shuffle_training_data = true;
//      p.score_training_samples = 100;
//      p.score_validation_samples = 50;
//      NNModel mymodel = p.initModel();
//      p.trainModel(mymodel);
//      p.trainModel(mymodel);
//      p.delete();

      // No leaks
      p.force_load_balance = false;
      p.balance_classes = false;
      p.shuffle_training_data = false;
      p.score_training_samples = 0;
      p.score_validation_samples = 0;
      p.execImpl();
    }

    // score and check result
    {
      NNModel mymodel = UKV.get(dest); //this actually *requires* frame to also still be in UKV (because of DataInfo...)
      Frame pred = mymodel.score(frame);
      water.api.ConfusionMatrix CM = new water.api.ConfusionMatrix();
      CM.actual = frame;
      CM.vactual = frame.vecs()[1];
      CM.predict = pred;
      CM.vpredict = pred.vecs()[0];
      CM.serve();
      StringBuilder sb = new StringBuilder();
      double error = CM.toASCII(sb);
      Log.info(sb);
//      if (error != 0) {
//        Assert.fail("Classification error is not 0, but " + error + ".");
//      }
      pred.delete();
      mymodel.delete();
    }

    frame.delete();
  }
}