package nexi.sengoku.easy;

import java.util.concurrent.Callable;

public abstract class AbstractTask<T> implements Callable<T> {

	protected final Context context;
	
	public AbstractTask(Context context) {
		this.context = context;
	}
}
