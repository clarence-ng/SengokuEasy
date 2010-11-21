package nexi.sengoku.easy;

public abstract class AbstractTask implements Runnable {

	protected final Context context;
	
	public AbstractTask(Context context) {
		this.context = context;
	}
}
