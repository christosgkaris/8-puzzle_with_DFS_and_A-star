/**
* Set up for the 15-Puzzle game (https://en.wikipedia.org/wiki/15_puzzle).
* Here you'll find what you need to code and practice search algorithms
* using Scala.
*/

object Puzzle {
	/**
	* Tile is an algebraic data type composed of nine other types.
	* Each represent a tile of the puzzle, including the empty one.
	*/
	sealed abstract class Tile
	case object Empty extends Tile
	case object One extends Tile
	case object Two extends Tile
	case object Three extends Tile
	case object Four extends Tile
	case object Five extends Tile
	case object Six extends Tile
	case object Seven extends Tile
	case object Eight extends Tile

	/**
	* Puzzle is a list of tiles.
	*/
	type Puzzle = List[Tile]

	/**
	* Given a Puzzle, return a list of new puzzles, that is, a list of
	* new possible states. These are generated by "moving" tiles according to
	* the restrictions of the puzzle.
	*/
	def unfold(p: Puzzle): List[Puzzle] = {
		/**
		* Swap the position of two elements given their index in the list.
		*/
		def swap(p: Puzzle, i1: Int, i2: Int): Puzzle = {
			require(i1 < i2, "i1 should be an index lower than i2")
			val (a, h1 :: b) = p.splitAt(i1)
			val (c, h2 :: d) = b.splitAt(i2 - i1 - 1)
			a ::: (h2 :: c) ::: (h1 :: d)
		}

		var children: List[Puzzle] = Nil
		val i = p.indexWhere(_ == Empty)
		val row = i / 3
		val col = i - (3 * row)

		if (col < 2) children = swap(p, i, i + 1) :: children	// right
		if (col > 0) children = swap(p, i - 1, i) :: children	// left
		if (row < 2) children = swap(p, i, i + 3) :: children	// bottom
		if (row > 0) children = swap(p, i - 3, i) :: children	// top

		children
	}

	/**
	* Returns true if the given puzzle represents a solution, false otherwise.
	*/
	def isSolution(p: Puzzle): Boolean =
		p.filter(_ != Empty) == List(One, Two, Three, Four, Five, Six, Seven, Eight)

	/**
	* Generates a new random puzzle.
	*/
	def newPuzzle: Puzzle = scala.util.Random.shuffle {
		List(Empty, One, Two, Three, Four, Five, Six, Seven, Eight)
	}

	// ===========================================
	// ------------ My code starts here ----------

	// DFS
	// List(Three, One, Two, Six, Four, Five, Seven, Eight, Empty) // 437 moves ~ 1 seconds
	// List(One, Eight, Two, Empty, Four, Three, Seven, Six, Five) // 11832 moves ~ 5 seconds
	// List(Five, Two, Eight, Four, One, Seven, Empty, Three, Six) // 41131 moves ~ 75 seconds
	// List(Empty, Eight, Seven, Four, Three, Six, Two, One, Five) // 68121 moves ~ 210 seconds
	// List(Eight, One, Two, Empty, Four, Three, Seven, Six, Five) // 9!/2 moves ~ 50 minutes - unsolvable

	// A*
	// List(Three, One, Two, Six, Four, Five, Seven, Eight, Empty) // 376 moves ~ 1 seconds
	// List(One, Eight, Two, Empty, Four, Three, Seven, Six, Five) // 218 moves ~ 1 seconds
	// List(Five, Two, Eight, Four, One, Seven, Empty, Three, Six) // 138 moves ~ 1 seconds
	// List(Empty, Eight, Seven, Four, Three, Six, Two, One, Five) // 336 moves ~ 1 seconds
	// List(Eight, One, Two, Empty, Four, Three, Seven, Six, Five) // 9!/2 moves ~ 50 minutes - unsolvable
	
	
	// DFS
	def depthFirst(root: Puzzle): (String, Puzzle, List[Puzzle]) = {

		/* Search if puzzle state is already visited */
		def checked(state: Puzzle, visited: List[Puzzle]): Boolean = visited match {
			case Nil => false
			case h :: t => if (h == state) true else checked(state, t)
		}

		/* Return the next state of the stack */
		def newStack(stack: List[Puzzle], visited: List[Puzzle]): List[Puzzle] = stack match {
			case Nil => Nil
			case h :: t => if (!checked(h, visited))   /* is the head not visited? */
						   		h :: t                 /* then return the stack as it is */
						   else                        /* else find the first unvisited head */
						   		newStack(t, visited)
		}

		/* Recursive execution */
		def dfs(stack: List[Puzzle], visited: List[Puzzle]): (String, Puzzle, List[Puzzle]) = {
			if ((!stack.isEmpty) && (isSolution(stack.head))) { /* if the top of the stack is the solution */
				println() ; println() ; print("States explored: ") ; println(1 + visited.length) ; println()
				("There is solution", stack.head, visited :+ stack.head)
			}
			else if ((!stack.isEmpty) && (!isSolution(stack.head))) /* basic step */
				dfs(newStack(unfold(stack.head) ::: stack, visited :+ stack.head), visited :+ stack.head)
			else /* if the stack gets empty, there is no solution (this will take about 63 minutes to end) */
				("There is no solution", Nil, visited)
		}

		if (isSolution(root)) /* if the puzzle is already solved */
			("There is solution", root, root :: Nil)
		else
			dfs(unfold(root), root :: Nil) /* the first search step */
	}

	// A*
	def heuristic(root: Puzzle): (String, Puzzle, List[Puzzle]) = {

		/* Search if puzzle state is already visited */
		def checked(state: Puzzle, visited: List[Puzzle]): Boolean = visited match {
			case Nil => false
			case h :: t => if (h == state) true else checked(state, t)
		}

		/* Return the next state of the stack */
		def newStack(stack: List[Puzzle], visited: List[Puzzle]): List[Puzzle] = stack match {
			case Nil => Nil
			case h :: t => if (!checked(h, visited))   /* is the head not visited? */
						   		h :: t                 /* then return the stack as it is */
						   else                        /* else find the first unvisited head */
						   		newStack(t, visited)
		}

		/* H(n) function */
		def h(nextSt: List[Puzzle]): List[Puzzle] = {

			/* Returns the manhattan value of a puzzle */
			def manhattan(p: Puzzle): Int = {	
				
				/* Returns the manhattan value of a tile */
				def mBlock(t: Tile, pos: Int): Int = {
					if (t == One)
						if (pos == 1) 0
						else if ((pos == 2) || (pos == 4)) 1
						else if ((pos == 3) || (pos == 5) || (pos == 7)) 2
						else if ((pos == 6) || (pos == 8)) 3
						else 4
					else if (t == Two)
						if (pos == 2) 0
						else if ((pos == 1) || (pos == 3) || (pos == 5)) 1
						else if ((pos == 4) || (pos == 6) || (pos == 8)) 2
						else 3
					else if (t == Three)
						if (pos == 3) 0
						else if ((pos == 2) || (pos == 6)) 1
						else if ((pos == 1) || (pos == 5) || (pos == 9)) 2
						else if ((pos == 4) || (pos == 8)) 3
						else 4
					else if (t == Four)
						if (pos == 4) 0
						else if ((pos == 1) || (pos == 5) || (pos == 7)) 1
						else if ((pos == 2) || (pos == 6) || (pos == 8)) 2
						else 3
					else if (t == Five)
						if (pos == 5) 0
						else if ((pos == 2) || (pos == 4) || (pos == 6) || (pos == 8)) 1
						else 2
					else if (t == Six)
						if (pos == 6) 0
						else if ((pos == 3) || (pos == 5) || (pos == 9)) 1
						else if ((pos == 2) || (pos == 4) || (pos == 8)) 2
						else 3
					else if (t == Seven)
						if (pos == 7) 0
						else if ((pos == 4) || (pos == 8)) 1
						else if ((pos == 1) || (pos == 5) || (pos == 9)) 2
						else if ((pos == 2) || (pos == 6)) 3
						else 4
					else if (t == Eight)
						if (pos == 8) 0
						else if ((pos == 5) || (pos == 7) || (pos == 9)) 1
						else if ((pos == 2) || (pos == 4) || (pos == 6)) 2
						else 3
					else 0
				}

				/* calcutating the sum of the manhattan distances */
				mBlock(p.head, 1) + 
				mBlock(p.tail.head, 2) + 
				mBlock(p.tail.tail.head, 3) + 
				mBlock(p.tail.tail.tail.head, 4) + 
				mBlock(p.tail.tail.tail.tail.head, 5) + 
				mBlock(p.tail.tail.tail.tail.tail.head, 6) + 
				mBlock(p.tail.tail.tail.tail.tail.tail.head, 7) + 
				mBlock(p.tail.tail.tail.tail.tail.tail.tail.head, 8) + 
				mBlock(p.tail.tail.tail.tail.tail.tail.tail.tail.head, 9)
			}

			/* Returns the unfold result sorted from smaller to greater manhattan value */
			def sort(l: List[Int], st: List[Puzzle]): List[Puzzle] = l match {
				case Nil => Nil
				case a :: Nil => st
				case a :: b :: Nil => if (a <= b) st else st.reverse
				case a :: b :: c :: Nil => 
					if ((a <= b) && (b <= c)) st
					else if ((a <= c) && (c <= b)) 
						st.head :: st.tail.tail.head :: st.tail.head :: Nil
					else if ((b <= a) && (a <= c))
						st.tail.head :: st.head :: st.tail.tail.head :: Nil
					else if ((b <= c) && (c <= a))
						st.tail.head :: st.tail.tail.head :: st.head :: Nil
					else if ((c <= a) && (a <= b))
						st.tail.tail.head :: st.head :: st.tail.head :: Nil
					else st.reverse
				case a :: b :: c :: d :: Nil => 
					if ((a <= b) && (b <= c) && (c <= d)) st
					else if ((a <= b) && (b <= d) && (d <= c))
					 	st.head :: st.tail.head :: st.tail.tail.tail.head :: st.tail.tail.head :: Nil
					else if ((a <= c) && (c <= b) && (b <= d))
					 	st.head :: st.tail.tail.head :: st.tail.head :: st.tail.tail.tail.head :: Nil
					else if ((a <= c) && (c <= d) && (d <= b))
					 	st.head :: st.tail.tail.head :: st.tail.tail.tail.head :: st.tail.head :: Nil
					else if ((a <= d) && (d <= b) && (b <= c))
					 	st.head :: st.tail.tail.tail.head :: st.tail.head :: st.tail.tail.head :: Nil
					else if ((a <= d) && (d <= c) && (c <= b))
					 	st.head :: st.tail.tail.tail.head :: st.tail.tail.head :: st.tail.head :: Nil
					else if ((b <= a) && (a <= c) && (c <= d)) 
					 	st.tail.head :: st.head :: st.tail.tail.head :: st.tail.tail.tail.head :: Nil
					else if ((b <= a) && (a <= d) && (d <= c))
					 	st.tail.head :: st.head :: st.tail.tail.tail.head :: st.tail.tail.head :: Nil
					else if ((b <= c) && (c <= a) && (a <= d))
					 	st.tail.head :: st.tail.tail.head :: st.head :: st.tail.tail.tail.head :: Nil
					else if ((b <= c) && (c <= d) && (d <= a))
					 	st.tail.head :: st.tail.tail.head :: st.tail.tail.tail.head :: st.head :: Nil
					else if ((b <= d) && (d <= a) && (a <= c))
					 	st.tail.head :: st.tail.tail.tail.head :: st.head :: st.tail.tail.head :: Nil
					else if ((b <= d) && (d <= c) && (c <= a))
					 	st.tail.head :: st.tail.tail.tail.head :: st.tail.tail.head :: st.head :: Nil
					else if ((c <= a) && (a <= b) && (b <= d)) 
					 	st.tail.tail.head :: st.head :: st.tail.head :: st.tail.tail.tail.head :: Nil
					else if ((c <= a) && (a <= d) && (d <= b))
					 	st.tail.tail.head :: st.head :: st.tail.tail.tail.head :: st.tail.head :: Nil
					else if ((c <= b) && (b <= a) && (a <= d))
					 	st.tail.tail.head :: st.tail.head :: st.head :: st.tail.tail.tail.head :: Nil
					else if ((c <= b) && (b <= d) && (d <= a))
					 	st.tail.tail.head :: st.tail.head :: st.tail.tail.tail.head :: st.head :: Nil
					else if ((c <= d) && (d <= a) && (a <= b))
					 	st.tail.tail.head :: st.tail.tail.tail.head :: st.head :: st.tail.head :: Nil
					else if ((c <= d) && (d <= b) && (b <= a))
					 	st.tail.tail.head :: st.tail.tail.tail.head :: st.tail.head :: st.head :: Nil
					else if ((d <= a) && (a <= b) && (b <= c)) 
					 	st.tail.tail.tail.head :: st.head :: st.tail.head :: st.tail.tail.head :: Nil
					else if ((d <= a) && (a <= c) && (c <= b))
					 	st.tail.tail.tail.head :: st.head :: st.tail.tail.head :: st.tail.head :: Nil
					else if ((d <= b) && (b <= a) && (a <= c))
					 	st.tail.tail.tail.head :: st.tail.head :: st.head :: st.tail.tail.head :: Nil
					else if ((d <= b) && (b <= c) && (c <= a))
					 	st.tail.tail.tail.head :: st.tail.head :: st.tail.tail.head :: st.head :: Nil
					else if ((d <= c) && (c <= a) && (a <= b))
						st.tail.tail.tail.head :: st.tail.tail.head :: st.head :: st.tail.head :: Nil
					else st.reverse
				case a :: b :: c :: d :: e => Nil
			}

			/* unfold gives back always 1 or 2 or 3 or 4 states */
			if (nextSt.length == 0)	List(Nil) // impossible scenario, written for the compiler
			else if (nextSt.length == 1)
				sort(manhattan(nextSt.head) :: Nil, nextSt)
			else if (nextSt.length == 2)
				sort(manhattan(nextSt.head) :: 
					manhattan(nextSt.tail.head) :: Nil, nextSt)
			else if (nextSt.length == 3)
				sort(manhattan(nextSt.head) :: 
					manhattan(nextSt.tail.head) :: 
					manhattan(nextSt.tail.tail.head) :: Nil, nextSt)
			else if (nextSt.length == 4)
				sort(manhattan(nextSt.head) :: 
					manhattan(nextSt.tail.head) :: 
					manhattan(nextSt.tail.tail.head) :: 
					manhattan(nextSt.tail.tail.tail.head) :: Nil, nextSt)
			else List(Nil) // impossible scenario, written for the compiler not to complain
		}

		/* Recursive execution */
		def heur(stack: List[Puzzle], visited: List[Puzzle]): (String, Puzzle, List[Puzzle]) = {
			if ((!stack.isEmpty) && (isSolution(stack.head))) { /* if the top of the stack is the solution */
				println() ; println() ; print("States explored: ") ; println(1 + visited.length) ; println()
				("There is solution", stack.head, visited :+ stack.head)
			}
			else if ((!stack.isEmpty) && (!isSolution(stack.head))) /* basic step */
				heur(newStack(h(unfold(stack.head)) ::: stack, visited :+ stack.head), visited :+ stack.head)
			else /* if the stack gets empty, there is no solution (this will take about 63 minutes to end) */
				("There is no solution", Nil, visited)
		}

		if (isSolution(root)) /* if the puzzle is already solved */
			("There is solution", root, root :: Nil)
		else
			heur(unfold(root), root :: Nil) /* the first search step */
	}

}
