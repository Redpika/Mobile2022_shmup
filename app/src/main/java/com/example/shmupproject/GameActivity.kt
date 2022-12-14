package com.example.shmupproject

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.text.Typography.bullet


@Suppress("DEPRECATION")
class GameActivity : AppCompatActivity() {
    private lateinit var scoreGame : TextView
    private lateinit var gameOverScore: TextView
    private lateinit var playerShip : ImageView
    private var enemyShip = mutableMapOf<ImageView, MutableList<Float>>()
    private var playerBullet = mutableMapOf<ImageView, MutableList<Float>>()
    private var enemyBullet = mutableMapOf<ImageView, MutableList<Float>>()
    private var bomb = mutableMapOf<ImageView, MutableList<Float>>()
    private var explosion = mutableMapOf<ImageView, MutableList<Int>>()
    private lateinit var mainLayout: ViewGroup

    private var screenHeight = 0
    private var screenWidth = 0

    private var enemyShipSpeed: Float = 0f
    private var bulletSpeed: Float = 0f
    private var bombSpeed: Float = 0f

    private var score = 0
    private var bombCounter = 0

    private var xDelta = 0
    private var yDelta = 0

    private var deathState = false
    private var deathTimer = 100
    private var gameOverState = false

    var timerHandler = Handler()
    var moveRunnable: Runnable = object : Runnable {
        override fun run() {
            if (deathState && deathTimer > 50) {
                deathTimer--
                timerHandler.postDelayed(this, 20)
            }
            else if (deathState && deathTimer > 0) {
                changePos()
                enemyShoot()
                timerHandler.postDelayed(this, 40)
            }
            else if (!gameOverState) {
                changePos()
                enemyShoot()
                timerHandler.postDelayed(this, 20)
            }
        }
    }
    /*var spawnRunnable: Runnable = object : Runnable {
        override fun run() {
            spawnShip()
            timerHandler.postDelayed(this, 2000)
        }
    }*/
    var bulletRunnable: Runnable = object : Runnable {
        override fun run() {
            if (!gameOverState) {
                playerShoot()
                timerHandler.postDelayed(this, 500)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        mainLayout = findViewById(R.id.game_layout)
        scoreGame = findViewById(R.id.score_game)

        scoreGame.text = "Score: $score"

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenHeight = displayMetrics.heightPixels
        screenWidth = displayMetrics.widthPixels

        enemyShipSpeed = (screenHeight / 300f).roundToInt().toFloat()
        bulletSpeed = (screenHeight / 240f).roundToInt().toFloat()
        bombSpeed = (screenHeight / 260f).roundToInt().toFloat()
        bombCounter = floor(Math.random() * 10).toInt()

        playerShip = ImageView(this)
        playerShip.setImageResource(R.drawable.shipplayer)
        playerShip.layoutParams = LinearLayout.LayoutParams(100, 200)
        playerShip.x = 500F
        playerShip.y = 1800F
        mainLayout.addView(playerShip)
        playerShip.setOnTouchListener(onTouchListener())

        for (i in 1..10) {
            val newShip = ImageView(this)
            newShip.setImageResource(R.drawable.shipenemy)
            newShip.layoutParams = LinearLayout.LayoutParams(100, 200)
            var newX = 0f
            while (true) {
                newX = floor((Math.random() * 10) * screenWidth / 10 - newShip.width / 2).toFloat()
                if (newX + newShip.width < screenWidth) {
                    break
                }
            }
            val newY = floor(-(Math.random() * 1000 + newShip.height)).toFloat()
            newShip.x = newX
            newShip.y = newY
            val downTimer = 0f
            val shootTimer = floor(Math.random() * 100).toFloat()
            val list = mutableListOf<Float>()
            list.add(newX)
            list.add(newY)
            list.add(downTimer)
            list.add(shootTimer)
            enemyShip.put(newShip, list)
            mainLayout.addView(newShip)
        }

        for (i in 1 .. 10) {
            val newBullet = ImageView(this)
            newBullet.setImageResource(R.drawable.bulletplayer)
            newBullet.layoutParams = LinearLayout.LayoutParams(50,50)
            val x = screenWidth.toFloat() + 100f
            val y = 0f
            newBullet.x = x
            newBullet.y = y
            val stat = 0f
            val list = mutableListOf<Float>()
            list.add(x)
            list.add(y)
            list.add(stat)
            playerBullet.put(newBullet, list)
            mainLayout.addView(newBullet)
        }
        for (i in 1 .. 20) {
            val newBullet = ImageView(this)
            newBullet.setImageResource(R.drawable.bulletenemy)
            newBullet.layoutParams = LinearLayout.LayoutParams(50,50)
            val x = screenWidth.toFloat() + 100f
            val y = 0f
            newBullet.x = x
            newBullet.y = y
            val stat = 0f
            val list = mutableListOf<Float>()
            list.add(x)
            list.add(y)
            list.add(stat)
            enemyBullet.put(newBullet, list)
            mainLayout.addView(newBullet)
        }
        for (i in 1 .. 2) {
            val newBomb = ImageView(this)
            newBomb.setImageResource(R.drawable.powerupbomb)
            newBomb.layoutParams = LinearLayout.LayoutParams(100,100)
            val x = screenWidth.toFloat() + 100f
            val y = 0f
            newBomb.x = x
            newBomb.y = y
            val stat = 0f
            val list = mutableListOf<Float>()
            list.add(x)
            list.add(y)
            list.add(stat)
            bomb.put(newBomb, list)
            mainLayout.addView(newBomb)
        }
        for (i in 1 .. 11) {
            val newExplosion = ImageView(this)
            newExplosion.setImageResource(R.drawable.explosion)
            newExplosion.layoutParams = LinearLayout.LayoutParams(200,200)
            newExplosion.x = screenWidth + 100f
            newExplosion.y = 0f
            val stat = 0
            val timer = 100
            val list = mutableListOf<Int>()
            list.add(stat)
            list.add(timer)
            explosion.put(newExplosion, list)
            mainLayout.addView(newExplosion)
        }


        timerHandler.postDelayed(moveRunnable, 0)
        timerHandler.postDelayed(bulletRunnable, 0)
    }

    private fun changePos() {
        collision()
        for((ship, pos) in enemyShip) {
            if (pos[2] == 0f) {
                pos[1] += enemyShipSpeed
                if (pos[1] > screenHeight) {
                    pos[0] = floor((Math.random() * 10) * screenWidth / 10 + ship.width).toFloat()
                    pos[1] = floor(-(Math.random() * 1000 + ship.height)).toFloat()
                    pos[2] = 20f
                }
                ship.x = pos[0]
                ship.y = pos[1]
            }
            else {
                pos[2] -= 1f
            }
        }
        for ((bullet, pos) in playerBullet) {
            if (pos[2] == 1f) {
                pos[1] -= bulletSpeed
                if (pos[1] < 0 - bullet.height) {
                    pos[0] = -100f
                    pos[1] = -100f
                    pos[2] = 0f
                }
                bullet.x = pos[0]
                bullet.y = pos[1]
            }
        }
        for ((bullet, pos) in enemyBullet) {
            if (pos[2] == 1f) {
                pos[1] += bulletSpeed
                if (pos[1] > screenHeight) {
                    pos[0] = -100f
                    pos[1] = -100f
                    pos[2] = 0f
                }
                bullet.x = pos[0]
                bullet.y = pos[1]
            }
        }
        for ((image, stat) in bomb) {
            if (stat[2] == 1f) {
                stat[1] += bombSpeed
                if (stat[1] > screenHeight) {
                    stat[0] = screenWidth + 100f
                    stat[1] = 0f
                    stat[2] = 0f
                }
                image.x = stat[0]
                image.y = stat[1]
            }
        }
        for ((image, stat) in explosion) {
            if (stat[0] == 1) {
                if (stat[1] == 0) {
                    stat[0] = 0
                    stat[1] = 100
                    image.x = screenWidth.toFloat() + 100f
                    image.y = 0f
                } else {
                    stat[1] -= 1
                }
            }
        }
        if (deathState) {
            deathTimer--
            if(deathTimer == 0) {
                deathState = false
                gameOverState = true
            }
        }
        if (gameOverState) {
            timerHandler.removeCallbacks(moveRunnable)
            timerHandler.removeCallbacks(bulletRunnable)
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.popup_design,null)

            val wid = LinearLayout.LayoutParams.WRAP_CONTENT
            val high = LinearLayout.LayoutParams.WRAP_CONTENT
            val focus = true
            val popUpWindow = PopupWindow(popupView, wid, high, focus)

            popUpWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0)

            gameOverScore = popupView.findViewById(R.id.game_over_score)
            gameOverScore.text = "Score: " + score
            val sharedPreferences = getSharedPreferences("GAME_DATA", Context.MODE_PRIVATE)
            val highScore = sharedPreferences.getInt("HIGH_SCORE", 0)

            if (score > highScore) {
                // Update HighScore
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putInt("HIGH_SCORE", score)
                editor.apply()
            }

            val exitButton: Button = popupView.findViewById(R.id.exitButton)
            exitButton.setOnClickListener {
                popUpWindow.dismiss()
                finish()
            }
        }
    }

    private fun playerShoot() {
        for ((bullet, pos) in playerBullet) {
            if (pos[2] == 0f) {
                pos[0] = playerShip.x + playerShip.width/2 - bullet.width/2
                pos[1] = playerShip.y - bullet.height
                pos[2] = 1f
                break
            }
        }
    }

    private fun enemyShoot() {
        for ((ship, pos) in enemyShip) {
            if (pos[3] == 0f && pos[1] > 0) {
                for ((bullet, posB) in enemyBullet) {
                    if (posB[2] == 0f) {
                        posB[0] = ship.x + ship.width/2 - bullet.width/2
                        posB[1] = ship.y + ship.height + bullet.height
                        posB[2] = 1f
                        break
                    }
                }
                pos[3] = 100f
            }
            else if (pos[1] > 0) {
                pos[3] -= 1f
            }
        }
    }

    private fun collision() {
        for ((bullet, pos) in playerBullet) {
            var collided = false
            for ((ship, posS) in enemyShip) {
                if (pos[0] < posS[0] + ship.width && pos[0]+bullet.width > posS[0]
                    && pos[1] < posS[1] + ship.height && pos[1]+bullet.height > posS[1]) {
                    for ((image, stat) in explosion) {
                        if (stat[0] == 0){
                            image.x = posS[0] - 50f
                            image.y = posS[1]
                            stat[0] = 1
                            break
                        }
                    }
                    pos[0] = -100f
                    pos[1] = 0f
                    pos[2] = 0f
                    bullet.x = pos[0]
                    bullet.y = pos[1]

                    bombCounter--
                    if (bombCounter == 0) {
                        for ((image, stat) in bomb) {
                            if (stat[2] == 0f) {
                                stat[0] = posS[0]
                                stat[1] = posS[1] + 50f
                                stat[2] = 1f

                                image.x = stat[0]
                                image.y = stat[1]

                                bombCounter = floor(Math.random() * 10).toInt()
                                break
                            }
                        }
                    }

                    posS[0] = floor((Math.random() * 10) * screenWidth / 10 + ship.width).toFloat()
                    posS[1] = floor(-(Math.random() * 1000 + ship.height)).toFloat()
                    posS[2] = 20f
                    ship.x = posS[0]
                    ship.y = posS[1]

                    collided = true
                    if (!deathState && !gameOverState) {
                        score++
                        scoreGame.text = "Score: " + score
                    }
                    break
                }
            }
            if (collided) {
                break
            }
        }

        for ((bullet, pos) in enemyBullet) {
            if (pos[0] < playerShip.x + playerShip.width && pos[0] + bullet.width > playerShip.x
                && pos[1] < playerShip.y + playerShip.height && pos[1] + bullet.height > playerShip.y) {
                for ((image, stat) in explosion) {
                    if (stat[0] == 0){
                        image.x = playerShip.x - 50f
                        image.y = playerShip.y
                        stat[0] = 1
                        break
                    }
                }
                pos[0] = -100f
                pos[1] = 0f
                pos[2] = 0f
                bullet.x = pos[0]
                bullet.y = pos[1]

                playerShip.x = screenWidth + 300f
                playerShip.y = 0f
                deathState = true

                break
            }
        }

        for ((ship, pos) in enemyShip) {
            if (pos[0] < playerShip.x + playerShip.width && pos[0] + ship.width > playerShip.x
                && pos[1] < playerShip.y + playerShip.height && pos[1] + ship.height > playerShip.y) {
                for ((image, stat) in explosion) {
                    if (stat[0] == 0){
                        image.x = playerShip.x - 50f
                        image.y = playerShip.y
                        stat[0] = 1
                        break
                    }
                }
                playerShip.x = screenWidth + 300f
                playerShip.y = 0f
                deathState = true

                break
            }
        }

        for ((image, pos) in bomb) {
            if (pos[0] < playerShip.x + playerShip.width && pos[0] + image.width > playerShip.x
                && pos[1] < playerShip.y + playerShip.height && pos[1] + image.height > playerShip.y) {
                for ((ship, posS) in enemyShip) {
                    if (posS[1] + ship.height > 0) {
                        for ((imageE, stat) in explosion) {
                            if (stat[0] == 0){
                                imageE.x = posS[0] - 50f
                                imageE.y = posS[1]
                                stat[0] = 1
                                break
                            }
                        }
                        posS[0] = floor((Math.random() * 10) * screenWidth / 10 + ship.width).toFloat()
                        posS[1] = floor(-(Math.random() * 1000 + ship.height)).toFloat()
                        posS[2] = 20f
                        ship.x = posS[0]
                        ship.y = posS[1]

                        if (!deathState && !gameOverState) {
                            score++
                            scoreGame.text = "Score: $score"
                        }
                    }
                }

                for ((bullet, posB) in enemyBullet) {
                    if (posB[2] == 1f) {
                        posB[0] = screenWidth + 100f
                        posB[1] = 0f
                        posB[2] = 0f

                        bullet.x = posB[0]
                        bullet.y = posB[1]
                    }
                }
                pos[0] = screenWidth + 100f
                pos[1] = 0f
                pos[2] = 0f

                image.x = pos[0]
                image.y = pos[1]
            }
        }
    }

    /*private fun spawnShip() {
        val wave_amount = floor(Math.random() * 5 + 1).toInt()
        for (i in 1 until wave_amount) {
            val newShip = ImageView(this)
            newShip.setImageResource(R.drawable.shipenemy)
            newShip.layoutParams = LinearLayout.LayoutParams(100, 200)
            val newX = floor(Math.random() * screenWidth + newShip.width).toFloat()
            val newY = 20f
            newShip.x = newX
            newShip.y = newY
            val list = mutableListOf<Float>()
            list.add(newX)
            list.add(newY)
            enemyShip.put(newShip, list)
            mainLayout?.addView(newShip)
        }
    }*/

    private fun onTouchListener(): View.OnTouchListener {
        return View.OnTouchListener { view, event ->
            // position information
            // about the event by the user
            val x = event.rawX.toInt()
            val y = event.rawY.toInt()
            // detecting user actions on moving
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    val lParams = view.layoutParams as RelativeLayout.LayoutParams
                    xDelta = x - lParams.leftMargin
                    yDelta = y - lParams.topMargin
                }
                MotionEvent.ACTION_MOVE -> {
                    // based on x and y coordinates (when moving image)
                    // and image is placed with it.
                    val layoutParams = view.layoutParams as RelativeLayout.LayoutParams
                    layoutParams.leftMargin = x - xDelta
                    layoutParams.topMargin = y - yDelta
                    layoutParams.rightMargin = 0
                    layoutParams.bottomMargin = 0
                    view.layoutParams = layoutParams
                }
            }
            // reflect the changes on screen
            mainLayout.invalidate()
            true
        }
    }

    override fun onBackPressed() {}
}