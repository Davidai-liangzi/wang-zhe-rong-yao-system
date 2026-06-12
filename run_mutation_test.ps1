# Mutation Testing Engine v2
# Creates 20 mutants, compiles each, runs BoundaryTest + TestRunner, reports survivors
# Compatible with src/main/java/ structure

$ErrorActionPreference = "Continue"
$cwd = Get-Location
# If $cwd doesn't match script dir, use script dir
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path -ErrorAction SilentlyContinue
if ($scriptDir) { $cwd = $scriptDir }
Set-Location $cwd

$srcDir   = "$cwd\src\main\java"
$bakDir   = "$cwd\_mutant_backup"
$outDir   = "$cwd\out"
$junitJar = "$cwd\junit-console.jar"

# Ensure backup exists
if (-not (Test-Path "$bakDir\service\SearchService.java")) {
    Write-Host "ERROR: Backup not found. Run backup creation first." -ForegroundColor Red
    exit 1
}

function Restore-File($relPath) {
    Copy-Item "$bakDir\$relPath" "$srcDir\$relPath" -Force
}

function Apply-Mutant($relPath, $searchRegex, $replacement) {
    $content = Get-Content "$srcDir\$relPath" -Raw
    $newContent = $content -replace $searchRegex, $replacement
    if ($content -eq $newContent) {
        Write-Host "  WARNING: Regex did not match!" -ForegroundColor Yellow
        return $false
    }
    [System.IO.File]::WriteAllText((Join-Path $srcDir $relPath), $newContent, (New-Object System.Text.UTF8Encoding $false))
    return $true
}

function Invoke-Test($mutantId) {
    Write-Host "  Compiling..." -NoNewline
    
    # Collect all Java files: src/main/java/** + src/test/java/** + root *.java
    $allJava = @(Get-ChildItem -Path "$cwd\src" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName })
    $allJava += @(Get-ChildItem -Path "$cwd" -Filter "*.java" | ForEach-Object { $_.FullName })
    
    Remove-Item -Recurse -Force "$outDir" -ErrorAction SilentlyContinue
    New-Item -ItemType Directory -Force "$outDir" | Out-Null
    
    $compile = & javac -encoding UTF-8 -d "$outDir" -cp "$junitJar" $allJava 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host " COMPILE FAILED" -ForegroundColor Red
        return "COMPILE_FAIL"
    }
    Write-Host " OK" -NoNewline

    # Run BoundaryTest (JUnit 5)
    Write-Host " BoundaryTest..." -NoNewline
    $bResult = & java -jar "$junitJar" --classpath "$outDir" --select-class BoundaryTest 2>&1
    $bExit = $LASTEXITCODE
    
    # Run TestRunner
    Write-Host " TestRunner..." -NoNewline
    $tResult = & java -cp "$outDir" TestRunner 2>&1
    $tExit = $LASTEXITCODE

    $allOutput = ($bResult + $tResult) -join "`n"
    $allOutput | Out-File "$cwd\_mutant_output\$mutantId.txt" -Encoding UTF8

    if ($bExit -ne 0 -or $tExit -ne 0) {
        Write-Host " KILLED" -ForegroundColor Green
        return "KILLED"
    } else {
        Write-Host " SURVIVED" -ForegroundColor Red
        return "SURVIVED"
    }
}

New-Item -ItemType Directory -Force -Path "$cwd\_mutant_output" | Out-Null
Remove-Item "$cwd\_mutant_output\*.txt" -Force -ErrorAction SilentlyContinue

# =============================================================
# 20 Mutants
# =============================================================
$mutants = @(
    # M01: equipScore sign flip (adds price instead of subtracts)
    @{Id="M01"; Name="equipScore sign flip: -price -> +price";
      File="service\SearchService.java";
      Search='getBonusAtk\(\) \* 1\.0 \+ e\.getBonusDef\(\) \* 0\.8 \+ e\.getBonusHp\(\) \* 0\.6 - e\.getPrice\(\) \* 0\.001';
      Replace='getBonusAtk() * 1.0 + e.getBonusDef() * 0.8 + e.getBonusHp() * 0.6 + e.getPrice() * 0.001'},

    # M02: equipScore swap ATK and HP weights
    @{Id="M02"; Name="equipScore: swap ATK/HP weights (ATK*0.6, HP*1.0)";
      File="service\SearchService.java";
      Search='getBonusAtk\(\) \* 1\.0 \+ e\.getBonusDef\(\) \* 0\.8 \+ e\.getBonusHp\(\) \* 0\.6';
      Replace='getBonusAtk() * 0.6 + e.getBonusDef() * 0.8 + e.getBonusHp() * 1.0'},

    # M03: equipScore remove price penalty
    @{Id="M03"; Name="equipScore: remove price term entirely";
      File="service\SearchService.java";
      Search=' - e\.getPrice\(\) \* 0\.001';
      Replace=''},

    # M04: playerScore winRate weight x10
    @{Id="M04"; Name="playerScore: winRate weight 1.0->10.0";
      File="service\SearchService.java";
      Search='getWinRate\(\) \* 1\.0 \+ rankToScore';
      Replace='getWinRate() * 10.0 + rankToScore'},

    # M05: playerScore rank weight 5.0->0.5
    @{Id="M05"; Name="playerScore: rank weight 5.0->0.5";
      File="service\SearchService.java";
      Search='rankToScore\(p\.getRank\(\)\) \* 5\.0';
      Replace='rankToScore(p.getRank()) * 0.5'},

    # M06: playerScore remove matchesPlayed term
    @{Id="M06"; Name="playerScore: remove matchesPlayed term entirely";
      File="service\SearchService.java";
      Search=' \+ p\.getMatchesPlayed\(\) \* 0\.01';
      Replace=''},

    # M07: rankToScore King=5->1
    @{Id="M07"; Name="rankToScore: King=5->1 (best rank becomes worst)";
      File="service\SearchService.java";
      Search='"King": return 5;';
      Replace='"King": return 1;'},

    # M08: rankToScore swap Star(4) and Diamond(3) — 2-pass apply
    @{Id="M08"; Name="rankToScore: swap Star(4) and Diamond(3) scores";
      File="service\SearchService.java"; Pass=1;
      Search='"Star": return 4;';
      Replace='"Star": return 3;'},
    @{Id="M08"; Name="rankToScore: swap Star(4) and Diamond(3) scores (pass2)";
      File="service\SearchService.java"; Pass=2;
      Search='"Diamond": return 3;';
      Replace='"Diamond": return 4;'},

    # M09: rankToScore default 1->5
    @{Id="M09"; Name="rankToScore: default return 1->5 (unknown ranks=King level)";
      File="service\SearchService.java";
      Search='default: return 1;';
      Replace='default: return 5;'},

    # M10: scoreToRankName King threshold 4.5->3.0
    @{Id="M10"; Name="scoreToRankName: King threshold 4.5->3.0 (easier King)";
      File="service\SearchService.java";
      Search='if \(score >= 4\.5\) return "King"';
      Replace='if (score >= 3.0) return "King"'},

    # M11: scoreToRankName flip thresholds (reversed ranking)
    @{Id="M11"; Name="scoreToRankName: flip thresholds (reversed ranking)";
      File="service\SearchService.java";
      Search='if \(score >= 4\.5\) return "King";\s+if \(score >= 3\.5\) return "Star";\s+if \(score >= 2\.5\) return "Diamond";\s+if \(score >= 1\.5\) return "Platinum";\s+return "Gold";';
      Replace='if (score >= 4.5) return "Gold";' + "`r`n" + '        if (score >= 3.5) return "Platinum";' + "`r`n" + '        if (score >= 2.5) return "Diamond";' + "`r`n" + '        if (score >= 1.5) return "Star";' + "`r`n" + '        return "King";'},

    # M12: isSuitable TANK/WARRIOR swapped
    @{Id="M12"; Name="isSuitable: TANK also allows ATTACK+JUNGLE (WARRIOR behavior)";
      File="service\SearchService.java";
      Search='case TANK:\s+return type == EquipmentType\.DEFENSE \|\| type == EquipmentType\.MOVEMENT;';
      Replace='case TANK:' + "`r`n" + '                return type == EquipmentType.ATTACK || type == EquipmentType.JUNGLE || type == EquipmentType.MOVEMENT;'},

    # M13: combat defense multiplier 0.6->0.0
    @{Id="M13"; Name="combat: defense multiplier 0.6->0.0 (defense ignored)";
      File="service\CombatSimulator.java";
      Search='atk - \(int\)\(targetDef \* 0\.6\)';
      Replace='atk - (int)(targetDef * 0.0)'},

    # M14: combat remove Math.max floor
    @{Id="M14"; Name="combat: remove Math.max(1,...) floor (damage can be <=0)";
      File="service\CombatSimulator.java";
      Search='int baseDmg = Math\.max\(1, atk - \(int\)\(targetDef \* 0\.6\)\)';
      Replace='int baseDmg = atk - (int)(targetDef * 0.6)'},

    # M15: combat crit damage x1.5->x3.0
    @{Id="M15"; Name="combat: critical hit damage x1.5->x3.0";
      File="service\CombatSimulator.java";
      Search='dmg \* 1\.5\)';
      Replace='dmg * 3.0)'},

    # M16: combat dodge always 90%
    @{Id="M16"; Name="combat: dodge chance always 90% (near-impossible to hit)";
      File="service\CombatSimulator.java";
      Search='int dodgeChance = hasDefEq \? 15 : 10;';
      Replace='int dodgeChance = 90;'},

    # M17: combat remove random fluctuation
    @{Id="M17"; Name="combat: remove random fluctuation";
      File="service\CombatSimulator.java";
      Search='int dmg = baseDmg \+ rng\.nextInt\(11\) - 5;';
      Replace='int dmg = baseDmg;'},

    # M18: equipScoreForHero TANK weights all=1.0 (no role specialisation)
    @{Id="M18"; Name="equipScoreForHero: TANK weights all=1.0 (no role specialisation)";
      File="service\RecommendationService.java";
      Search='case TANK:\s+atkW = 0\.3; defW = 1\.5; hpW = 1\.2; break;   // Prioritize defense';
      Replace='case TANK:' + "`r`n" + '                atkW = 1.0; defW = 1.0; hpW = 1.0; break;   // Prioritize defense'},

    # M19: equipScoreForHero SUPPORT ATK weight 0.2->2.0
    @{Id="M19"; Name="equipScoreForHero: SUPPORT ATK weight 0.2->2.0 (attack-oriented support)";
      File="service\RecommendationService.java";
      Search='case SUPPORT:\s+defW = 1\.2; hpW = 1\.0; atkW = 0\.2; break;   // Prioritize survival';
      Replace='case SUPPORT:' + "`r`n" + '                defW = 0.1; hpW = 0.1; atkW = 2.0; break;   // Prioritize survival'},

    # M20: recommendHeroes sort reversed
    @{Id="M20"; Name="getRecommendedHeroesForPlayer: sort reversed (weakest first)";
      File="service\RecommendationService.java";
      Search='Integer\.compare\(\s+b\.getHp\(\) \+ b\.getAtk\(\) \+ b\.getDef\(\),\s+a\.getHp\(\) \+ a\.getAtk\(\) \+ a\.getDef\(\)\s*\)';
      Replace='Integer.compare(a.getHp() + a.getAtk() + a.getDef(), b.getHp() + b.getAtk() + b.getDef())'}
)

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "   Mutation Testing Engine v2" -ForegroundColor Cyan
Write-Host "   Tests: BoundaryTest (JUnit5) + TestRunner" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$results = @()
$totalMutants = ($mutants | Where-Object { -not $_.Pass -or $_.Pass -eq 1 }).Count
$i = 0
$lastId = ""

foreach ($m in $mutants) {
    if ($m.Pass -eq 2) { continue }  # handled by pass=1 logic below
    
    $i++
    Write-Host "[$i/$totalMutants] $($m.Id): $($m.Name)" -ForegroundColor White
    
    # Restore all originals
    Restore-File "service\SearchService.java"
    Restore-File "service\CombatSimulator.java"
    Restore-File "service\RecommendationService.java"
    Restore-File "service\DataManager.java"
    
    # Apply mutant (pass 1)
    $applied = Apply-Mutant $m.File $m.Search $m.Replace
    
    # Check if there's a pass=2 for same ID
    $pass2 = $mutants | Where-Object { $_.Id -eq $m.Id -and $_.Pass -eq 2 }
    if ($pass2) {
        $applied2 = Apply-Mutant $pass2.File $pass2.Search $pass2.Replace
        $applied = $applied -or $applied2
    }
    
    if (-not $applied) {
        $results += [PSCustomObject]@{ Id = $m.Id; Name = $m.Name; Status = "REGEX_FAIL" }
        Write-Host ""
        continue
    }
    
    # Compile + test
    $status = Invoke-Test $m.Id
    $results += [PSCustomObject]@{ Id = $m.Id; Name = $m.Name; Status = $status }
    Write-Host ""
}

# Restore originals
Restore-File "service\SearchService.java"
Restore-File "service\CombatSimulator.java"
Restore-File "service\RecommendationService.java"
Restore-File "service\DataManager.java"

# Final recompile
Write-Host "Restoring original code and recompiling..." -ForegroundColor Yellow
$allJava2 = @(Get-ChildItem -Path "$cwd\src" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName })
$allJava2 += @(Get-ChildItem -Path "$cwd" -Filter "*.java" | ForEach-Object { $_.FullName })
Remove-Item -Recurse -Force "$outDir" -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force "$outDir" | Out-Null
& javac -encoding UTF-8 -d "$outDir" -cp "$junitJar" $allJava2 2>&1 | Out-Null
Write-Host "Recompile complete. Running final verification..." -ForegroundColor Green

# Final verification
$v1 = & java -jar "$junitJar" --classpath "$outDir" --select-class BoundaryTest 2>&1 | Select-Object -Last 5
$v2 = & java -cp "$outDir" TestRunner 2>&1 | Select-Object -Last 5
Write-Host "BoundaryTest: $($v1 -join ' ')" -ForegroundColor Cyan
Write-Host "TestRunner: $($v2 -join ' ')" -ForegroundColor Cyan

# Report
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "   MUTATION TESTING RESULTS" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

$survivors = $results | Where-Object { $_.Status -eq "SURVIVED" }
$killed = $results | Where-Object { $_.Status -eq "KILLED" }
$compileFails = $results | Where-Object { $_.Status -eq "COMPILE_FAIL" }
$regexFails = $results | Where-Object { $_.Status -eq "REGEX_FAIL" }

Write-Host ""
Write-Host "KILLED (detected by tests):     $($killed.Count)/$totalMutants" -ForegroundColor Green
foreach ($k in $killed) {
    Write-Host "  [OK] $($k.Id): $($k.Name)" -ForegroundColor Green
}

Write-Host ""
Write-Host "SURVIVED (undetected):          $($survivors.Count)/$totalMutants" -ForegroundColor Red
foreach ($s in $survivors) {
    Write-Host "  [!!] $($s.Id): $($s.Name)" -ForegroundColor Red
}

if ($compileFails.Count -gt 0) {
    Write-Host ""
    Write-Host "COMPILE FAILED:                 $($compileFails.Count)/$totalMutants" -ForegroundColor DarkYellow
    foreach ($c in $compileFails) {
        Write-Host "  [--] $($c.Id): $($c.Name)" -ForegroundColor DarkYellow
    }
}

if ($regexFails.Count -gt 0) {
    Write-Host ""
    Write-Host "REGEX FAILED (no match):        $($regexFails.Count)/$totalMutants" -ForegroundColor DarkYellow
    foreach ($r in $regexFails) {
        Write-Host "  [??] $($r.Id): $($r.Name)" -ForegroundColor DarkYellow
    }
}

Write-Host ""
$score = [math]::Round($killed.Count/$totalMutants*100, 1)
Write-Host "Mutation score: $($killed.Count)/$totalMutants ($score%)" -ForegroundColor Cyan

if ($score -ge 90) {
    Write-Host "Rating: EXCELLENT - Test suite is very effective!" -ForegroundColor Green
} elseif ($score -ge 75) {
    Write-Host "Rating: GOOD - Test suite detects most mutations." -ForegroundColor Yellow
} elseif ($score -ge 50) {
    Write-Host "Rating: ADEQUATE - Consider adding more precise tests." -ForegroundColor DarkYellow
} else {
    Write-Host "Rating: WEAK - Test suite needs significant improvement." -ForegroundColor Red
}
