package fastcampus.aop.part2.practice_07_audiorecoder

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private val soundVisualizerView: SoundVisualizerView by lazy {
        findViewById(R.id.soundVisualizerView)
    }
    private val recordTimeTextView: CountUpView by lazy {
        findViewById(R.id.recordTimeTextView)
    }
    private val resetButton: Button by lazy {
        findViewById(R.id.resetButton)
    }
    private val recordButton: RecordButton by lazy {
        findViewById(R.id.recordButton)
    }
    private val requiredPermission = arrayOf(android.Manifest.permission.RECORD_AUDIO)

    private val recordingFilePath: String by lazy {
        "${externalCacheDir?.absolutePath}/recording.3gp"
    }
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var state = State.BEFORE_RECORDING
        set(value) {
            field = value
            resetButton.isEnabled = (value == State.AFTER_RECODING) ||
                    (value == State.ON_PLAYING)
            recordButton.updateIconWidthState(value)
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestAudioPermission()
        initViews()
        bindViews()
        initVariables()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val audioRecordPermissionGranted = requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
                grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED


        if (!audioRecordPermissionGranted) {
            finish()
        }
    }

    private fun requestAudioPermission() {
        requestPermissions(requiredPermission, REQUEST_RECORD_AUDIO_PERMISSION)

    }

    private fun initViews() {
        recordButton.updateIconWidthState(state)
    }

    private fun bindViews() {
        soundVisualizerView.onRequestCurrentAmplitude = {
            recorder?.maxAmplitude ?: 0
        }
        resetButton.setOnClickListener {
            stopPlaying()
            soundVisualizerView.clearVisualization()
            recordTimeTextView.clearCountTime()
            state = State.BEFORE_RECORDING
        }
        recordButton.setOnClickListener {
            when (state) {
                State.BEFORE_RECORDING -> {
                    startRecording()
                }
                State.ON_RECODING -> {
                    stopRecording()
                }
                State.AFTER_RECODING -> {
                    startPlaying()
                }
                State.ON_PLAYING -> {
                    stopPlaying()
                }
            }
        }
    }

    private fun initVariables() {
        state = State.BEFORE_RECORDING
    }

    private fun startRecording() {
        recorder = MediaRecorder()
            .apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(recordingFilePath)
                prepare()
            }
        recorder?.start()
        soundVisualizerView.startVisualizing(false)
        recordTimeTextView.startCountUp()
        state = State.ON_RECODING
    }

    private fun stopRecording() {
        recorder?.run {
            stop()
            release()
        }
        recorder = null
        soundVisualizerView.stopVisualizing()
        recordTimeTextView.stopCountUp()
        state = State.AFTER_RECODING
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            setDataSource(recordingFilePath)
            prepare()
        }
        player?.setOnCompletionListener {
            stopPlaying()
            state = State.AFTER_RECODING
        }
        player?.start()
        soundVisualizerView.startVisualizing(true)
        recordTimeTextView.startCountUp()
        state = State.ON_PLAYING
    }

    private fun stopPlaying() {
        player?.release()
        player = null
        soundVisualizerView.stopVisualizing()
        recordTimeTextView.stopCountUp()
        state = State.AFTER_RECODING
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 201
    }
}