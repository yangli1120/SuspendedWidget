package per.learn.demosuspendedwidget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, SuspendedWidgetService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.small_widget_btn: {
                Intent i = new Intent(this, SuspendedWidgetService.class);
                i.putExtra(SuspendedWidgetService.KEY_CMD_SUSPENDED_WIDGET,
                        SuspendedWidgetService.CMD_START_SMALL_SUSPENDED_WIDGET);

                startService(i);
            }break;

            case R.id.remove_small_widget_btn: {
                Intent i = new Intent(this, SuspendedWidgetService.class);
                i.putExtra(SuspendedWidgetService.KEY_CMD_SUSPENDED_WIDGET,
                        SuspendedWidgetService.CMD_REMOVE_SMALL_SUSPENDED_WIDGET);

                startService(i);
            }break;

            case R.id.big_widget_btn: {
                Intent i = new Intent(this, SuspendedWidgetService.class);
                i.putExtra(SuspendedWidgetService.KEY_CMD_SUSPENDED_WIDGET,
                        SuspendedWidgetService.CMD_START_BIG_SUSPENDED_WIDGET);

                startService(i);
            }break;

            case R.id.remove_big_widget_btn: {
                Intent i = new Intent(this, SuspendedWidgetService.class);
                i.putExtra(SuspendedWidgetService.KEY_CMD_SUSPENDED_WIDGET,
                        SuspendedWidgetService.CMD_REMOVE_BIG_SUSPENDED_WIDGET);

                startService(i);
            }break;
        }
    }

}
