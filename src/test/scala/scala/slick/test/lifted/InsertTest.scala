package scala.slick.test.lifted

import org.junit.Test
import org.junit.Assert._
import scala.slick.lifted._
import scala.slick.session.Database.threadLocalSession
import scala.slick.testutil._
import scala.slick.testutil.TestDB._

object InsertTest extends DBTestObject(H2Mem, SQLiteMem, Postgres, MySQL, DerbyMem, HsqldbMem, MSAccess, SQLServer)

class InsertTest(val tdb: TestDB) extends DBTest {
  import tdb.profile.Table
  import tdb.profile.Implicit._

  class TestTable(name: String) extends Table[(Int, String)](name) {
    def id = column[Int]("id")
    def name = column[String]("name")
    def * = id ~ name
  }

  object Src1 extends TestTable("src1")
  object Dst1 extends TestTable("dst1")
  object Dst2 extends TestTable("dst2")

  @Test def testSimple(): Unit = db withSession {

    (Src1.ddl ++ Dst1.ddl ++ Dst2.ddl).create

    Src1.insert(1, "A")
    Src1.insertAll((2, "B"), (3, "C"))

    Dst1.insertExpr(Src1)
    assertEquals(Set((1,"A"), (2,"B"), (3,"C")), Query(Dst1).list.toSet)

    val q2 = for(s <- Src1 if s.id <= 2) yield s
    println("Insert 2: "+Dst2.insertStatementFor(q2))
    Dst2.insert(q2)
    assertEquals(Set((1,"A"), (2,"B")), Query(Dst2).list.toSet)

    val q3 = 42 ~ "X".bind
    println("Insert 3: "+Dst2.insertStatementFor(q3))
    Dst2.insertExpr(q3)
    assertEquals(Set((1,"A"), (2,"B"), (42,"X")), Query(Dst2).list.toSet)

    val q4 = 43 ~ "Y".bind
    println("Insert 4: "+Dst2.shaped.insertStatementFor(q4))
    Dst2.shaped.insertExpr(q4)
    assertEquals(Set((1,"A"), (2,"B"), (42,"X"), (43,"Y")), Query(Dst2).list.toSet)
  }
}
