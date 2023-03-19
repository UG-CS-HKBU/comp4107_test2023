import kotlin.random.Random

interface Role {
    val roleTitle: String
    fun getEnemy(): String
}

class MonarchRole : Role {
    override val roleTitle = "Monarch"
    override fun getEnemy() = "Rebel, then Traitors"
}

class MinisterRole : Role {
    override val roleTitle = "Minister"
    override fun getEnemy() = "Rebel, then Traitors"
}

class RebelRole : Role {
    override val roleTitle = "Rebel"
    override fun getEnemy() = "Monarch"
}

class TraitorRole : Role {
    override val roleTitle = "Traitor"
    override fun getEnemy() = "Rebel, then Monarch"
}

abstract class Hero(var role: Role) : Role by role {
    abstract val name: String
    open val maxHP: Int = 4
    open var hp: Int = maxHP
    open var numOfCards = 4
    public var abandon = false
    var commands = mutableListOf<Command>()

    public var index = 0

    private fun getIndexOfLeftHero(numOfHeros:Int, dist: Int = 1): Int {
        return (index + numOfHeros - dist ) % numOfHeros;
    }

    private fun getIndexOfRightHero(numOfHeros: Int, dist: Int = 1): Int {
        return (index + dist) % numOfHeros;
    }

    fun setCommand(command: Command) {
        commands.add(command)
    }

    fun executeCommend() {
        while (commands.isNotEmpty()) {
            var c = commands.removeAt(0)
            c.execute()
        }
    }

    open fun templateMethod() {
        println("${name}'s turn:")
        executeCommend()
        if (!abandon) {
            drawCards()
            playCards()
        } else
            println("Sun Quan's round got abandoned.")
        discardCards()
        println()
    }

    open fun playCards() {
        val leftHero = heroes[getIndexOfLeftHero(heroes.size)];
        val rightHero = heroes[getIndexOfRightHero(heroes.size)];
        println("${leftHero.name} is on the left-hand side, and ${rightHero.name} is on the right-hand side.")
        attack()
    }

    open fun drawCards() {
        var n = 2
        numOfCards += n

        println("Drawing $n cards")
        println("$name now has $numOfCards cards.")
    }

    open fun discardCards() {
        if (hp >= numOfCards)
            println("Current HP is $hp, number of cards is $numOfCards. No need to discard cards.")
        else {
            println("Current HP is $hp, discarding ${numOfCards - hp} cards")
            numOfCards = hp
        }
    }

    open fun attack() {
        var enemies = getEnemy()
        numOfCards--
        println("$name is $roleTitle, spent 1 card to attack $enemies.")
    }

    open fun dodgeAttack(): Boolean {
        return false
    }

    open fun beingAttacked() {
        println("$name got attached")
        if (!dodgeAttack()) {
            hp--
            println("$name is unable to dodge attack, current hp is $hp.")
        } else {
            println("$name dodged attack, current hp is $hp.")
        }
    }
}

abstract class MonarchHero(role: Role) : Hero(role) {
    override val maxHP = 5
}

interface Handler {
    fun hasNext(): Boolean
    fun getNext(): Handler?
    fun setNext(h: Handler)
    fun handle(): Boolean
}

abstract class WeiHero(role: Role) : Hero(role), Handler {

    private var nextHandler: Handler? = null
    override fun hasNext() = nextHandler != null
    override fun setNext(h: Handler) {
        nextHandler = h
    }

    override fun getNext() = nextHandler;

    override fun handle(): Boolean {
        var result = false
        if (role is MinisterRole && numOfCards > 0) {
            println("$name spent 1 card to help the lord to dodge.")
            numOfCards--
            result = true
        } else {
            println("$name doesn't want to help.")
            if (hasNext())
                result = getNext()!!.handle()
        }
        return result
    }
}

abstract class WarriorHero(role: Role) : Hero(role) {
    override val maxHP = 4
}

abstract class AdvisorHero(role: Role) : Hero(role) {
    override val maxHP = 3
}

class LiuBei : MonarchHero(MonarchRole()) {
    override val name = "Liu Bei"
}

class CaoCao : MonarchHero(MonarchRole()) {
    override val name = "Cao Cao"
    var helper: Handler? = null

    override fun dodgeAttack(): Boolean {
        var result = false
        if (helper != null)
            result = helper!!.handle()

        if (!result)
            println("No one can help lord to dodge.")

        return result
    }
}

class SunQuan : MonarchHero(MonarchRole()) {
    override val name = "Sun Quan"
}

class SimaYi(role: Role) : WeiHero(role) {
    override val name = "Sima Yi"
    override val maxHP = 3
}

class XuChu(role: Role) : WeiHero(role) {
    override val name = "Xu Chu"
    override val maxHP = 4
}

class XiaHouyuan(role: Role) : WeiHero(role) {
    override val name = "XiaHou yuan"
    override val maxHP = 4
}

class ZhangFei(role: Role) : WarriorHero(role) {
    override val name = "Zhang Fei"

    override fun playCards() {
        while (numOfCards > 0)
            attack()

    }
}

class ZhouYu(role: Role) : AdvisorHero(role) {
    override val name = "Zhou Yu"

    override fun drawCards() {
        println("I'm handsome, so I can draw 3 cards.")
        numOfCards += 3
    }
}

class DiaoChan(role: Role) : AdvisorHero(role) {
    override val name = "Dia Chan"

    override fun discardCards() {
        super.discardCards()
        numOfCards++
        println("I can draw one more card, now I have $numOfCards cards.")
    }
}

class GuanYu {
    val name = "Guan Yu"
    fun getAttackString() = "Power 💪 !!"
}

class GuanYuAdapter(role: Role) : WarriorHero(role) {
    private var guanYu = GuanYu();
    override val name = guanYu.name

    override fun attack() {
        println(guanYu.getAttackString())
        super.attack()
    }
}

interface GameObjectFactory {
    fun getRandomRole(): Role
    fun createRandomHero(): Hero
}

val random = Random(0)

object MonarchFactory : GameObjectFactory {
    private val hero: Hero = when (random.nextInt(3)) {
        0 -> LiuBei()
        1 -> CaoCao()
        else -> SunQuan()
    }

    override fun getRandomRole(): Role {
        TODO("Unnecessary")
    }

    override fun createRandomHero(): Hero {
        return hero
    }

}

object NoneMonarchFactory : GameObjectFactory {
    private var classList =
        mutableListOf<String>("ZhangFei", "ZhouYu", "DiaoChan", "GuanYuAdapter", "SimaYi", "XuChu", "XiaHouyuan")

    override fun getRandomRole(): Role {

        return when (random.nextInt(3)) {
            0 -> MinisterRole()
            1 -> RebelRole()
            else -> TraitorRole()
        }
    }

    override fun createRandomHero(): Hero {
        if (classList.isEmpty()) throw NoSuchElementException("No hero anymore!")

        val thisPackage: Package? = this.javaClass.`package`
        val packageName = if (thisPackage == null) "" else thisPackage.name + "."
        val classname = packageName + classList.removeAt(random.nextInt(classList.size))

        val hero = Class.forName(classname).getConstructor(Role::class.java).newInstance(getRandomRole()) as Hero
        if (monarchHero is CaoCao && hero is WeiHero) {
            val cao = monarchHero as CaoCao
            if (cao.helper == null)
                cao.helper = hero as WeiHero
            else {
                var handler: Handler? = cao.helper as Handler

                while (handler!!.hasNext())
                    handler = handler.getNext()
                handler.setNext(hero as Handler)
            }
        }
        return hero
    }

}

interface Command {
    abstract fun execute()
}

class Abandon : Command {
    private lateinit var receiver: Hero

    constructor(receiver: Hero) {
        this.receiver = receiver
        println("${receiver.name} being placed the Abandon card.")
    }

    override fun execute() {
        receiver.abandon = random.nextBoolean()
        if (receiver.abandon)
            println("${receiver.name} being placed the Abandon card.")
    }
}


var monarchHero: MonarchHero = MonarchFactory.createRandomHero() as MonarchHero
var heroes = mutableListOf<Hero>();

fun play() {
    heroes.add(monarchHero)
    monarchHero.setCommand(Abandon(monarchHero))
    for (i in 0..2) {
        var hero = NoneMonarchFactory.createRandomHero()
        hero.index = heroes.size;
        heroes.add(hero)
    }

    for (hero in heroes) {
        hero.beingAttacked()
        hero.templateMethod()
    }
}


fun main(args: Array<String>) {
    play();
}
